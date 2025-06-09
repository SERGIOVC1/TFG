package com.tfg.tfg.services;

import com.tfg.tfg.persistance.model.WebDirectoryLog;
import com.tfg.tfg.persistance.repository.WebDirectoryLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
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
                String microserviceUrl = "https://e766-84-125-184-18.ngrok-free.app/scan/gobuster";
                String jsonInput = String.format("{\"target\":\"%s\"}", target);

                URL url = new URL(microserviceUrl);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json");
                con.setDoOutput(true);
                con.getOutputStream().write(jsonInput.getBytes(StandardCharsets.UTF_8));

                BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String publicIp = getPublicIp();
                String internalIp = InetAddress.getLocalHost().getHostAddress();
                String location = getLocation();

                // ✅ Corrección del patrón: doble backslash para evitar escape inválido
                Pattern resultPattern = Pattern.compile("^(\\/\\S+)\\s+\\(Status:\\s*(\\d{3})\\).*");
                String line;
                while ((line = reader.readLine()) != null) {
                    emitter.send(line);
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
                            log.setTimestamp(Instant.now().toEpochMilli());
                            log.setUserAgent(System.getProperty("http.agent"));
                            log.setIsBot(false);
                            log.setLocation(location);
                            logRepository.save(log);
                        }
                    }
                }

                reader.close();
                emitter.complete();
            } catch (Exception e) {
                try {
                    emitter.send("Error: " + e.getMessage());
                } catch (IOException ignored) {}
                emitter.completeWithError(e);
            }
        }).start();

        return emitter;
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
                return (city + ", " + country).trim();
            }
        } catch (Exception e) {
            return "Desconocida";
        }
    }
}
