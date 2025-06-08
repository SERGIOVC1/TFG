package com.tfg.tfg.services;

import com.tfg.tfg.persistance.model.HoleheLog;
import com.tfg.tfg.persistance.repository.HoleheLogRepository;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
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
            URL url = new URL("http://4.233.136.111:3000/scan/holehe"); // tu IP de VM
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String json = "{\"email\": \"" + email + "\"}";
            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes(StandardCharsets.UTF_8));
            }

            StringBuilder resultData = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");

                    if (line.trim().startsWith("[+]") &&
                        !line.contains("Email used") &&
                        !line.contains("Email not used") &&
                        !line.contains("Rate limit")) {
                        resultData.append(line.trim()).append(", ");
                    }
                }
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
                log.setTimestamp(Instant.now());
                log.setUserAgent(System.getProperty("http.agent"));
                log.setIsBot(false);
                log.setLocation(getLocation());

                holeheLogRepository.save(log);
            }

        } catch (Exception e) {
            output.append("❌ Error al ejecutar holehe: ").append(e.getMessage());
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
                        ? jsonStr.replaceAll(".*\"city\":\"([^\"]+)\".*", "$1") : "";
                String country = jsonStr.matches(".*\"country_name\":\"[^\"]+\".*")
                        ? jsonStr.replaceAll(".*\"country_name\":\"([^\"]+)\".*", "$1") : "";

                return (city + ", " + country).trim().isEmpty() ? "Desconocida" : (city + ", " + country).trim();
            }
        } catch (Exception e) {
            return "Desconocida";
        }
    }
}
