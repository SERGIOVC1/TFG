// WebDirectoryScannerService.java
package com.tfg.tfg.services;

import com.tfg.tfg.persistance.model.WebDirectoryLog;
import com.tfg.tfg.persistance.repository.WebDirectoryLogRepository;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.*;
import java.net.InetAddress;
import java.net.URL;
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
                File gobusterFile = new ClassPathResource("bin/gobuster.exe").getFile();
                File wordlistFile = new ClassPathResource("wordlists/custom-wordlist-5000.txt").getFile();

                String command = "\"" + gobusterFile.getAbsolutePath() + "\" dir --url " + target +
                        " --wordlist \"" + wordlistFile.getAbsolutePath() + "\" -x php,html -t 2";

                ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", command);
                pb.redirectErrorStream(true);
                Process process = pb.start();

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                Pattern ansiPattern = Pattern.compile("\u001B\\[[;\\d]*m|\u001B\\[2K");
                Pattern resultPattern = Pattern.compile("^(\\/\\S+)\\s+\\(Status:\\s*(\\d{3})\\).*");

                String publicIp = getPublicIp();
                String internalIp = InetAddress.getLocalHost().getHostAddress();
                String location = getLocation();

                String line;
                while ((line = reader.readLine()) != null) {
                    line = ansiPattern.matcher(line).replaceAll("");
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
                            log.setTimestamp(System.currentTimeMillis());
                            log.setUserAgent(System.getProperty("http.agent"));
                            log.setIsBot(false);
                            log.setLocation(location);

                            logRepository.save(log);
                        }
                    }
                }

                process.waitFor();
                emitter.complete();
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
                String city = jsonStr.matches(".*\"city\":\"[^\"]+\".*") ? jsonStr.replaceAll(".*\"city\":\"([^\"]+)\".*", "$1") : "";
                String country = jsonStr.matches(".*\"country_name\":\"[^\"]+\".*") ? jsonStr.replaceAll(".*\"country_name\":\"([^\"]+)\".*", "$1") : "";
                String location = (city + ", " + country).trim();
                return location.isBlank() ? "Desconocida" : location;
            }
        } catch (Exception e) {
            return "Desconocida";
        }
    }
}
