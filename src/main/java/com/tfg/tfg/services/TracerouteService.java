package com.tfg.tfg.services;

// package com.tfg.tfg.services;

import com.tfg.tfg.persistance.model.TracerouteLog;
import com.tfg.tfg.persistance.model.TracerouteLogResult;
import com.tfg.tfg.persistance.repository.TracerouteLogRepository;
import com.tfg.tfg.persistance.repository.TracerouteLogResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class TracerouteService {

    @Autowired
    private TracerouteLogRepository tracerouteLogRepository;

    @Autowired
    private TracerouteLogResultRepository tracerouteLogResultRepository;

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

                String loc = (city + ", " + country).trim();
                return loc.isBlank() ? "Desconocida" : loc;
            }
        } catch (Exception e) {
            return "Desconocida";
        }
    }

    public List<String> executeTraceroute(String target, String userId) {
        List<String> tracerouteOutput = new ArrayList<>();

        // Simulación del traceroute (reemplaza por ejecución real si tienes)
        tracerouteOutput.add("142.250.184.174 Madrid, Spain Google");
        tracerouteOutput.add("212.166.147.222 Madrid, Spain Desconocido");
        tracerouteOutput.add("108.170.252.253 Montreal, Canada Google");

        try {
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

            TracerouteLog savedLog = tracerouteLogRepository.save(log);

            List<TracerouteLogResult> results = new ArrayList<>();
            for (String line : tracerouteOutput) {
                String[] parts = line.split(" ", 4);
                if (parts.length == 4) {
                    TracerouteLogResult res = new TracerouteLogResult();
                    res.setIp(parts[0]);
                    res.setCity(parts[1].replace(",", ""));
                    res.setCountry(parts[2]);
                    res.setProvider(parts[3]);
                    res.setTracerouteLog(savedLog);
                    results.add(res);
                }
            }
            tracerouteLogResultRepository.saveAll(results);

        } catch (Exception e) {
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
