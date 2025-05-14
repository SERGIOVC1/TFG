package com.tfg.tfg.services;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

@Service
public class TechnologyScannerService {

    public Map<String, String> analyzeWebsite(String inputUrl) {
        Map<String, String> techs = new LinkedHashMap<>();

        try {
            // ✅ Normalizar URL
            if (!inputUrl.startsWith("http://") && !inputUrl.startsWith("https://")) {
                inputUrl = "http://" + inputUrl;
            }

            URL initialUrl = new URL(inputUrl);
            HttpURLConnection connection = (HttpURLConnection) initialUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(7000);
            connection.setReadTimeout(7000);
            connection.setInstanceFollowRedirects(true);

            // ✅ Añadir un User-Agent como navegador real
            connection.setRequestProperty("User-Agent", 
              "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/113.0.0.0 Safari/537.36");

            int responseCode = connection.getResponseCode();

            // 🔁 Si redirige a HTTPS (3xx), seguirla
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
                    if (value.contains("XSRF-TOKEN")) techs.put("Framework", "Laravel");
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

        } catch (Exception e) {
            techs.put("Error", "❌ No se pudo analizar: " + e.getMessage());
        }

        return techs;
    }
}
