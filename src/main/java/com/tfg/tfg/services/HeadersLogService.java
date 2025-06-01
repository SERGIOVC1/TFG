package com.tfg.tfg.services;

import com.tfg.tfg.persistance.model.HeadersLog;
import com.tfg.tfg.persistance.model.HeadersLogResult;
import com.tfg.tfg.persistance.repository.HeadersLogRepository;
import com.tfg.tfg.persistance.repository.HeadersLogResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class HeadersLogService {

    @Autowired
    private HeadersLogRepository headersLogRepository;

    @Autowired
    private HeadersLogResultRepository headersLogResultRepository;

    // Método actualizado para recibir userId
    public Map<String, String> analyzeAndSave(String url, String userId) {
        Map<String, String> resultHeaders = null;

        try {
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://" + url;
            }

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = restTemplate.headForHeaders(url);

            String[] keys = {
                "Content-Security-Policy",
                "X-Content-Type-Options",
                "Strict-Transport-Security",
                "X-Frame-Options",
                "Referrer-Policy",
                "Permissions-Policy"
            };

            // Guardar log principal con userId
            HeadersLog log = new HeadersLog();
            log.setUrl(url);
            log.setUserId(userId != null ? userId : "desconocido"); // Guarda userId o "desconocido"
            log.setToolUsed("header_analysis");
            log.setTimestamp(System.currentTimeMillis());
            log.setUserAgent(System.getProperty("http.agent"));
            log.setBot(false);
            log.setIpAddress(getPublicIp());
            log.setInternalIpAddress(InetAddress.getLocalHost().getHostAddress());
            log.setLocation(getLocation());

            HeadersLog savedLog = headersLogRepository.save(log);

            // Guardar cada header y valor individual, limpiando texto
            List<HeadersLogResult> resultsList = new ArrayList<>();
            for (String key : keys) {
                String value = headers.getFirst(key);
                if (value == null) {
                    value = "❌ No establecido";
                }
                value = sanitizeUtf8(value);

                HeadersLogResult result = new HeadersLogResult();
                result.setHeader(key);
                result.setValue(value);
                result.setHeadersLog(savedLog);
                resultsList.add(result);
            }

            headersLogResultRepository.saveAll(resultsList);

            // Crear mapa para devolver
            resultHeaders = new java.util.HashMap<>();
            for (HeadersLogResult r : resultsList) {
                resultHeaders.put(r.getHeader(), r.getValue());
            }

        } catch (Exception e) {
            e.printStackTrace();
            resultHeaders = Map.of("error", "Error al analizar la URL: " + e.getMessage());
        }

        return resultHeaders;
    }

    private static String sanitizeUtf8(String input) {
        if (input == null) return null;
        return input.replaceAll("[^\\x00-\\x7F]", "");
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
                String city = jsonStr.matches(".*\"city\":\"[^\"]+\".*") ? jsonStr.replaceAll(".*\"city\":\"([^\"]+)\".*", "$1") : "";
                String country = jsonStr.matches(".*\"country_name\":\"[^\"]+\".*") ? jsonStr.replaceAll(".*\"country_name\":\"([^\"]+)\".*", "$1") : "";

                String loc = (city + ", " + country).trim();
                return loc.isBlank() ? "Desconocida" : loc;
            }
        } catch (Exception e) {
            return "Desconocida";
        }
    }
}
