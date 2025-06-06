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

@Service  // Marca la clase como servicio gestionado por Spring
public class TechnologyScannerService {

    @Autowired
    private TechLogRepository techLogRepository;  // Repositorio para guardar logs generales

    @Autowired
    private TechLogResultRepository techLogResultRepository;  // Repositorio para guardar resultados detallados

    /**
     * Analiza una web para detectar tecnologías usadas.
     * @param inputUrl URL del sitio a analizar
     * @param userId ID opcional del usuario que solicita el análisis
     * @return Mapa con categoría-tecnología detectada
     */
    public Map<String, String> analyzeWebsite(String inputUrl, String userId) {
        Map<String, String> techs = new LinkedHashMap<>();  // Guarda tecnologías detectadas en orden

        try {
            // Añade esquema http si no está presente en la URL
            if (!inputUrl.startsWith("http://") && !inputUrl.startsWith("https://")) {
                inputUrl = "http://" + inputUrl;
            }

            // Abre conexión HTTP al sitio web
            URL initialUrl = new URL(inputUrl);
            HttpURLConnection connection = (HttpURLConnection) initialUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(7000);
            connection.setReadTimeout(7000);
            connection.setInstanceFollowRedirects(true);

            // Define User-Agent para simular un navegador real
            connection.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/113.0.0.0 Safari/537.36");

            int responseCode = connection.getResponseCode();

            // Si el servidor responde con redirección HTTPS, sigue la redirección
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

            // Obtiene todas las cabeceras HTTP
            Map<String, List<String>> headers = connection.getHeaderFields();

            // Analiza cabeceras para detectar tecnologías comunes
            headers.forEach((key, values) -> {
                String value = String.join(" ", values).toLowerCase();

                // Busca tecnología backend en "X-Powered-By"
                if (key != null && key.toLowerCase().contains("x-powered-by")) {
                    if (value.contains("express")) techs.put("Backend", "Express.js");
                    if (value.contains("php")) techs.put("Backend", "PHP");
                    if (value.contains("asp.net")) techs.put("Backend", "ASP.NET");
                }

                // Detecta servidor web en cabecera "Server"
                if ("server".equalsIgnoreCase(key)) {
                    if (value.contains("apache")) techs.put("Web Server", "Apache");
                    if (value.contains("nginx")) techs.put("Web Server", "Nginx");
                    if (value.contains("litespeed")) techs.put("Web Server", "LiteSpeed");
                }

                // Busca frameworks o CMS en cookies
                if ("set-cookie".equalsIgnoreCase(key)) {
                    if (value.contains("wp-settings")) techs.put("CMS", "WordPress");
                    if (value.contains("xsrf-token")) techs.put("Framework", "Laravel");
                    if (value.contains("csrftoken")) techs.put("Framework", "Django");
                }
            });

            // Lee el contenido HTML de la página para detectar tecnologías frontend y CMS
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder htmlBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                htmlBuilder.append(line.toLowerCase());
            }
            String html = htmlBuilder.toString();

            // Detecta CMS comunes buscando strings específicos en HTML
            if (html.contains("wp-content")) techs.put("CMS", "WordPress");
            if (html.contains("drupal.settings")) techs.put("CMS", "Drupal");
            if (html.contains("joomla")) techs.put("CMS", "Joomla");

            // Detecta librerías JS y frameworks frontend
            if (html.contains("jquery")) techs.put("JS Library", "jQuery");
            if (html.contains("react")) techs.put("Frontend", "React");
            if (html.contains("vue")) techs.put("Frontend", "Vue.js");
            if (html.contains("angular")) techs.put("Frontend", "Angular");

            // Detecta herramientas de analítica web
            if (html.contains("gtag(") || html.contains("ga(")) techs.put("Analytics", "Google Analytics");
            if (html.contains("hotjar")) techs.put("Analytics", "Hotjar");

            // Detecta frameworks CSS
            if (html.contains("bootstrap")) techs.put("CSS Framework", "Bootstrap");
            if (html.contains("tailwindcss")) techs.put("CSS Framework", "Tailwind CSS");

            // Crea un log principal con info general del escaneo
            TechLog log = new TechLog();
            log.setUrl(inputUrl);
            log.setToolUsed("technology_scan");
            log.setTimestamp(Instant.now());
            log.setUserAgent(System.getProperty("http.agent"));
            log.setIsBot(false);
            log.setIpAddress(getPublicIp());  // IP pública del servidor que hace el escaneo
            log.setInternalIpAddress(InetAddress.getLocalHost().getHostAddress());  // IP local servidor
            log.setLocation(getLocation());  // Ubicación aproximada del servidor
            log.setAction("Scan");
            log.setDetails("Escaneo de tecnologías web");

            // Si se pasa userId, se guarda también
            if(userId != null) {
                log.setUserId(userId);
            }

            // Guarda el log principal en base de datos
            TechLog savedLog = techLogRepository.save(log);

            // Guarda cada tecnología detectada como un resultado individual asociado al log
            List<TechLogResult> resultsList = new ArrayList<>();
            for (Map.Entry<String, String> entry : techs.entrySet()) {
                TechLogResult result = new TechLogResult();
                result.setCategory(entry.getKey());      // Categoría (ej: "Frontend")
                result.setTechnology(entry.getValue());  // Tecnología detectada (ej: "React")
                result.setTechLog(savedLog);              // Relación con log principal
                resultsList.add(result);
            }
            techLogResultRepository.saveAll(resultsList);  // Guarda resultados en base de datos

        } catch (Exception e) {
            // En caso de error, agrega entrada con mensaje de error en el mapa y lo imprime
            techs.put("Error", "❌ No se pudo analizar: " + e.getMessage());
            e.printStackTrace();
        }

        return techs;  // Devuelve mapa con tecnologías detectadas (o error)
    }

    // Obtiene la IP pública del servidor
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

    // Obtiene la ubicación aproximada del servidor consultando una API externa
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

                // Extrae ciudad y país del JSON recibido
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
