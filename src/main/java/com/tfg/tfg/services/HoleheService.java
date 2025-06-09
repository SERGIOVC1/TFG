package com.tfg.tfg.services;

import com.tfg.tfg.persistance.model.HoleheLog;
import com.tfg.tfg.persistance.repository.HoleheLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Service
public class HoleheService {

    @Autowired
    private HoleheLogRepository holeheLogRepository;

    /**
     * Realiza una solicitud HTTP al microservicio de holehe para escanear el email.
     * @param email Email a verificar
     * @param request Petición HTTP original
     * @param userId ID del usuario
     * @return Resultado en texto plano
     */
    public String runHolehe(String email, HttpServletRequest request, String userId) {
        StringBuilder output = new StringBuilder();

        try {
            // 🌐 URL pública del microservicio holehe vía ngrok
            String microserviceUrl = "https://54bc-84-125-184-18.ngrok-free.app/scan/holehe";

            // Monta el JSON de entrada
            String jsonInputString = String.format("{\"email\": \"%s\"}", email);

            // Crea conexión HTTP
            URL url = new URL(microserviceUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);

            // Envía JSON
            try (var os = con.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Lee respuesta
            try (var reader = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            con.disconnect();

            // Guarda log si hubo salida válida
            if (!output.toString().isBlank()) {
                HoleheLog log = new HoleheLog();
                log.setUserId(userId);
                log.setAction("Email Scan");
                log.setDetails(email);
                log.setIpAddress(getPublicIp());
                log.setInternalIpAddress(InetAddress.getLocalHost().getHostAddress());
                log.setResult(output.toString());
                log.setToolUsed("holehe");
                log.setTimestamp(Instant.now());
                log.setUserAgent(request.getHeader("User-Agent"));
                log.setIsBot(false);
                log.setLocation(getLocation());

                holeheLogRepository.save(log);
            }

        } catch (Exception e) {
            output.append("❌ Error al consultar el microservicio holehe: ").append(e.getMessage());
            e.printStackTrace();
        }

        return output.toString();
    }

    private String getPublicIp() {
        try {
            URL url = new URL("https://api.ipify.org");
            try (BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()))) {
                return in.readLine();
            }
        } catch (Exception e) {
            return "Desconocida";
        }
    }

    private String getLocation() {
        try {
            URL url = new URL("https://ipapi.co/json/");
            try (BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()))) {
                StringBuilder json = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    json.append(line);
                }

                String jsonStr = json.toString();
                String city = jsonStr.matches(".*\"city\":\"[^\"]+\".*")
                        ? jsonStr.replaceAll(".*\"city\":\"([^\"]+)\".*", "$1")
                        : "";
                String country = jsonStr.matches(".*\"country_name\":\"[^\"]+\".*")
                        ? jsonStr.replaceAll(".*\"country_name\":\"([^\"]+)\".*", "$1")
                        : "";

                String loc = (city + ", " + country).trim();
                return loc.isBlank() ? "Desconocida" : loc;
            }
        } catch (Exception e) {
            return "Desconocida";
        }
    }
}
