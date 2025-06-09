package com.tfg.tfg.services;

import com.tfg.tfg.persistance.model.TracerouteLog;
import com.tfg.tfg.persistance.repository.TracerouteLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class TracerouteService {

    @Autowired
    private TracerouteLogRepository tracerouteLogRepository;

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
                String city = jsonStr.matches(".*\"city\":\"[^\"]+\".*") ?
                        jsonStr.replaceAll(".*\"city\":\"([^\"]+)\".*", "$1") : "";
                String country = jsonStr.matches(".*\"country_name\":\"[^\"]+\".*") ?
                        jsonStr.replaceAll(".*\"country_name\":\"([^\"]+)\".*", "$1") : "";
                String loc = (city + ", " + country).trim();
                return loc.isBlank() ? "Desconocida" : loc;
            }
        } catch (Exception e) {
            return "Desconocida";
        }
    }

    public List<String> executeTraceroute(String target, String userId) {
        List<String> tracerouteOutput = new ArrayList<>();
        try {
            ProcessBuilder pb = new ProcessBuilder("traceroute", target);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                tracerouteOutput.add(line);
            }
            reader.close();
            process.waitFor();

            TracerouteLog log = new TracerouteLog();
            log.setTarget(target);
            log.setResult(String.join("\n", tracerouteOutput));
            log.setToolUsed("traceroute");
            log.setTimestamp(Instant.now());
            log.setUserAgent(System.getProperty("http.agent"));
            log.setIsBot(false);
            log.setIpAddress(getPublicIp());
            log.setInternalIpAddress(InetAddress.getLocalHost().getHostAddress());
            log.setLocation(getLocation());
            log.setAction("Traceroute ejecutado");
            if (userId != null && !userId.isEmpty()) {
                log.setUserId(userId);
            }

            tracerouteLogRepository.save(log);

        } catch (Exception e) {
            tracerouteOutput.add("Error al ejecutar traceroute: " + e.getMessage());
            e.printStackTrace();
        }
        return tracerouteOutput;
    }

    public List<TracerouteLog> getLogsByUserId(String userId) {
        if (userId == null || userId.isEmpty()) {
            return tracerouteLogRepository.findAll();
        } else {
            return tracerouteLogRepository.findByUserId(userId);
        }
    }
}