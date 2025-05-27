package com.tfg.tfg.services;

import com.tfg.tfg.persistance.model.HoleheLog;
import com.tfg.tfg.persistance.repository.HoleheLogRepository;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.time.Instant;

@Service
public class HoleheService {

    private final HoleheLogRepository holeheLogRepository;

    public HoleheService(HoleheLogRepository holeheLogRepository) {
        this.holeheLogRepository = holeheLogRepository;
    }

    public String runHolehe(String email) {
        StringBuilder output = new StringBuilder();

        try {
            File batFile = new ClassPathResource("bin/holehe-wrapper.bat").getFile();
            String batPath = batFile.getAbsolutePath();

            ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", batPath, email);
            pb.redirectErrorStream(true);

            Process process = pb.start();

            StringBuilder resultData = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");

                    // ✅ Guardar solo líneas útiles
                    if (line.trim().startsWith("[+]") &&
                        !line.contains("Email used") &&
                        !line.contains("Email not used") &&
                        !line.contains("Rate limit")) {
                        resultData.append(line.trim()).append(", ");
                    }
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                output.append("⚠️ Error al ejecutar holehe. Código de salida: ").append(exitCode);
            }

            String result = resultData.toString().replaceAll(",\\s*$", "");

            if (!result.isEmpty()) {
                HoleheLog log = new HoleheLog();
                log.setAction("Email Scan");
                log.setDetails(email);
                log.setIpAddress(getPublicIp());
                log.setInternalIpAddress(InetAddress.getLocalHost().getHostAddress());
                log.setResult(result);
                log.setToolUsed("holehe");
                log.setTimestamp(Instant.ofEpochMilli(System.currentTimeMillis()));  // <-- Cambio aquí
                log.setUserAgent(System.getProperty("http.agent"));
                log.setIsBot(false);
                log.setLocation(getLocation());

                holeheLogRepository.save(log);
            }

        } catch (Exception e) {
            e.printStackTrace(); // 🔴 Imprime el error en consola
            output.append("❌ Error al ejecutar holehe: ").append(e.getMessage());
        }

        return output.toString();
    }

    private String getPublicIp() {
        try {
            var url = new java.net.URL("https://api.ipify.org");
            try (var in = new BufferedReader(new InputStreamReader(url.openStream()))) {
                return in.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace(); // opcional
            return "Desconocida";
        }
    }

    private String getLocation() {
        try {
            var url = new java.net.URL("https://ipapi.co/json/");
            try (var in = new BufferedReader(new InputStreamReader(url.openStream()))) {
                StringBuilder json = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    json.append(line);
                }

                String jsonStr = json.toString();

                String city = jsonStr.matches(".*\"city\":\"[^\"]+\".*") ?
                        jsonStr.replaceAll(".*\"city\":\"([^\"]+)\".*", "$1") : "";
                String country = jsonStr.matches(".*\"country_name\":\"[^\"]+\".*") ?
                        jsonStr.replaceAll(".*\"country_name\":\"([^\"]+)\".*", "$1") : "";

                String location = (city + ", " + country).trim();
                return location.isBlank() ? "Desconocida" : location;
            }
        } catch (Exception e) {
            e.printStackTrace(); // 🔴 Para ver por qué falla la localización
            return "Desconocida";
        }
    }
}
