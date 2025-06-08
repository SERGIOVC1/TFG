package com.tfg.tfg.services;

import com.tfg.tfg.persistance.model.WebDirectoryLog;
import com.tfg.tfg.persistance.repository.WebDirectoryLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class WebDirectoryScannerService {

    private final WebDirectoryLogRepository logRepository;

    public WebDirectoryScannerService(WebDirectoryLogRepository logRepository) {
        this.logRepository = logRepository;
    }

    public SseEmitter scanDirectories(String target, String userId) {
        SseEmitter emitter = new SseEmitter();

        new Thread(() -> {
            try {
                // Realiza la conexión a tu servidor en la VM
                URL url = new URL("http://4.233.138.85:3000/scan/gobuster"); // IP de tu VM
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                String json = "{\"url\": \"" + target + "\"}";
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(json.getBytes(StandardCharsets.UTF_8));
                }

                // Prepara lectura y parsing
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                Pattern ansiPattern = Pattern.compile("\u001B\\[[;\\d]*m|\u001B\\[2K");
                Pattern resultPattern = Pattern.compile("^(\\/\\S+)\\s+\\(Status:\\s*(\\d{3})\\).*");

                String publicIp = getPublicIp();
                String internalIp = InetAddress.getLocalHost().getHostAddress();
                String location = getLocation();

                String line;
                while ((line = reader.readLine()) != null) {
                    line = ansiPattern.matcher(line).replaceAll("");
                    emitter.send(line);  // Enviar a cliente en tiempo real

                    Matcher matcher = resultPattern.matcher(line);
                    if (matcher.find()) {
                        int status = Integer.parseInt(matcher.group(2));
                        if (status == 200 || status == 301 || status == 302 || status == 303) {
                            WebDirectoryLog log = new WebDirectoryLog();
                            log.setUserId(userId);
                            log.setAction("Directory Scan");
                            log.setDetails(target);
                            log.setIpAddress(publicIp);
                            log.setInternalIpAddress(internalIp);
                            log.setResult(line.trim());
                            log.setToolUsed("gobuster");
                            log.setTimestamp(System.currentTimeMillis());
                            log.setUserAgent(System.getProperty("http.agent"));
                            log.setIsBot(false);
                            log.setLocation(location);
                            logRepository.save(log);
                        }
                    }
                }

                emitter.complete();  // Finaliza la transmisión SSE
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        }).start();

        return emitter;
    }

    private String getPublicIp() {
        try {
            URL url = new URL("https://api.ipify.org");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
                return reader.readLine();
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

                return (city + ", " + country).trim().isBlank() ? "Desconocida" : (city + ", " + country).trim();
            }
        } catch (Exception e) {
            return "Desconocida";
        }
    }
}
