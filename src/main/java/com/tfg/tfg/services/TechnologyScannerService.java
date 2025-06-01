package com.tfg.tfg.services;

import com.tfg.tfg.persistance.model.TechLog;
import com.tfg.tfg.persistance.model.TechLogResult;
import com.tfg.tfg.persistance.repository.TechLogRepository;
import com.tfg.tfg.persistance.repository.TechLogResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.util.*;

@Service
public class TechnologyScannerService {

    @Autowired
    private TechLogRepository techLogRepository;

    @Autowired
    private TechLogResultRepository techLogResultRepository;

    public Map<String, String> analyzeWebsite(String inputUrl, String userId) {
        Map<String, String> techs = new LinkedHashMap<>();

        try {
            if (!inputUrl.startsWith("http://") && !inputUrl.startsWith("https://")) {
                inputUrl = "http://" + inputUrl;
            }

            URL initialUrl = new URL(inputUrl);
            HttpURLConnection connection = (HttpURLConnection) initialUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(7000);
            connection.setReadTimeout(7000);
            connection.setInstanceFollowRedirects(true);

            connection.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/113.0.0.0 Safari/537.36");

            int responseCode = connection.getResponseCode();

            if (responseCode >= 300 && responseCode < 400) {
                String redirected = connection.getHeaderField("Location");
                if (redirected != null && redirected.startsWith("https://")) {
                    connection = (HttpURLConnection) new URL(redirected).openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(7000);
                    connection.setReadTimeout(7000);
                    connection.setRequestProperty("User-Agent",
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/113.0.0.0 Safari/537.36");
                }
            }

            Map<String, List<String>> headers = connection.getHeaderFields();

            headers.forEach((key, values) -> {
                String value = String.join(" ", values).toLowerCase();

                if (key != null && key.toLowerCase().contains("x-powered-by")) {
                    if (value.contains("express")) techs.put("Backend", "Express.js");
                    if (value.contains("php")) techs.put("Backend", "PHP");
                    if (value.contains("asp.net")) techs.put("Backend", "ASP.NET");
                }

                if ("server".equalsIgnoreCase(key)) {
                    if (value.contains("apache")) techs.put("Web Server", "Apache");
                    if (value.contains("nginx")) techs.put("Web Server", "Nginx");
                    if (value.contains("litespeed")) techs.put("Web Server", "LiteSpeed");
                }

                if ("set-cookie".equalsIgnoreCase(key)) {
                    if (value.contains("wp-settings")) techs.put("CMS", "WordPress");
                    if (value.contains("xsrf-token")) techs.put("Framework", "Laravel");
                    if (value.contains("csrftoken")) techs.put("Framework", "Django");
                }
            });

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder htmlBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                htmlBuilder.append(line.toLowerCase());
            }

            String html = htmlBuilder.toString();

            if (html.contains("wp-content")) techs.put("CMS", "WordPress");
            if (html.contains("drupal.settings")) techs.put("CMS", "Drupal");
            if (html.contains("joomla")) techs.put("CMS", "Joomla");

            if (html.contains("jquery")) techs.put("JS Library", "jQuery");
            if (html.contains("react")) techs.put("Frontend", "React");
            if (html.contains("vue")) techs.put("Frontend", "Vue.js");
            if (html.contains("angular")) techs.put("Frontend", "Angular");

            if (html.contains("gtag(") || html.contains("ga(")) techs.put("Analytics", "Google Analytics");
            if (html.contains("hotjar")) techs.put("Analytics", "Hotjar");

            if (html.contains("bootstrap")) techs.put("CSS Framework", "Bootstrap");
            if (html.contains("tailwindcss")) techs.put("CSS Framework", "Tailwind CSS");

            // Guardar log principal
            TechLog log = new TechLog();
            log.setUrl(inputUrl);
            log.setToolUsed("technology_scan");
            log.setTimestamp(Instant.now());
            log.setUserAgent(System.getProperty("http.agent"));
            log.setIsBot(false);
            log.setIpAddress(getPublicIp());
            log.setInternalIpAddress(InetAddress.getLocalHost().getHostAddress());
            log.setLocation(getLocation());
            log.setAction("Scan");
            log.setDetails("Escaneo de tecnologías web");

            if(userId != null) {
                log.setUserId(userId);  // <-- Guarda userId si lo pasas
            }

            TechLog savedLog = techLogRepository.save(log);

            // Guardar resultados individuales
            List<TechLogResult> resultsList = new ArrayList<>();
            for (Map.Entry<String, String> entry : techs.entrySet()) {
                TechLogResult result = new TechLogResult();
                result.setCategory(entry.getKey());
                result.setTechnology(entry.getValue());
                result.setTechLog(savedLog);
                resultsList.add(result);
            }
            techLogResultRepository.saveAll(resultsList);

        } catch (Exception e) {
            techs.put("Error", "❌ No se pudo analizar: " + e.getMessage());
            e.printStackTrace();
        }

        return techs;
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

                String loc = (city + ", " + country).trim();
                return loc.isBlank() ? "Desconocida" : loc;
            }
        } catch (Exception e) {
            return "Desconocida";
        }
    }
}
