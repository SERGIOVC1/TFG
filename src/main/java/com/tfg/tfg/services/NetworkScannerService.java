package com.tfg.tfg.services;

import com.tfg.tfg.persistance.model.WebScannerLog;
import com.tfg.tfg.persistance.model.WebScannerResult;
import com.tfg.tfg.persistance.repository.WebScannerLogRepository;
import com.tfg.tfg.persistance.repository.WebScannerResultRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service  // Indica que esta clase es un servicio gestionado por Spring
public class NetworkScannerService {

    @Autowired
    private WebScannerLogRepository logRepository;  // Repositorio para guardar logs del escaneo

    @Autowired
    private WebScannerResultRepository resultRepository;  // Repositorio para guardar resultados detallados

    /**
     * Ejecuta un escaneo de red usando nmap, guarda log y resultados en base de datos.
     * @param target IP o URL a escanear
     * @param scanType Tipo de escaneo ("intermediate", "deep", etc.)
     * @param request Petición HTTP para obtener info adicional
     * @param userId ID del usuario que solicita el escaneo
     * @return Resultado completo del escaneo como texto
     * @throws Exception Si ocurre error en el proceso
     */
    public String scanNetwork(String target, String scanType, HttpServletRequest request, String userId) throws Exception {
        String ip = target;

        // Si target es una URL válida, la resuelve a IP
        if (isValidUrl(target)) {
            ip = resolveUrlToIp(target);
        }

        // Construye el comando nmap según tipo de escaneo y destino
        String command = buildCommand(scanType, ip);

        // Ejecuta el comando en el sistema operativo
        Process process = Runtime.getRuntime().exec(command);

        // Lee la salida estándar del proceso
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        process.waitFor();  // Espera que termine el proceso
        String result = output.toString();

        // Crea y guarda un log general del escaneo con info relevante
        WebScannerLog log = new WebScannerLog();
        log.setUserId(userId);  // Asocia el userId recibido al log
        log.setIpAddress(getPublicIp());  // IP pública del servidor que hace el escaneo
        log.setInternalIpAddress(InetAddress.getLocalHost().getHostAddress()); // IP local servidor
        log.setAction("Network Scan");
        log.setDetails(target);  // Target escaneado (IP o URL)
        log.setResult(result);  // Resultado completo del escaneo
        log.setToolUsed("network_scan");
        log.setTimestamp(Instant.now());
        log.setUserAgent(request.getHeader("User-Agent"));  // Info del cliente que hizo la petición
        log.setBot(false);  // Se marca que no es bot
        log.setLocation(getLocation());  // Ubicación geográfica aproximada del servidor

        WebScannerLog savedLog = logRepository.save(log);  // Guarda log en BD y obtiene entidad guardada

        // Procesa línea a línea el resultado para extraer información de puertos abiertos
        List<WebScannerResult> parsedResults = new ArrayList<>();

        for (String line : result.split("\n")) {
            // Regex para detectar líneas con formato: "80/tcp open http"
            Matcher matcher = Pattern.compile("(\\d+/tcp)\\s+(\\w+)\\s+(\\S+)").matcher(line);
            if (matcher.find()) {
                WebScannerResult entry = new WebScannerResult();
                entry.setLog(savedLog);  // Asocia resultado al log guardado
                entry.setPort(matcher.group(1));   // Puerto (ej: "80/tcp")
                entry.setState(matcher.group(2));  // Estado (ej: "open")
                entry.setService(matcher.group(3));  // Servicio (ej: "http")
                parsedResults.add(entry);
            }
        }

        resultRepository.saveAll(parsedResults);  // Guarda resultados detallados en BD

        return result;  // Devuelve resultado crudo del escaneo
    }

    /**
     * Resuelve una URL a su dirección IP.
     * @param url URL a resolver
     * @return IP correspondiente
     * @throws Exception si falla resolución DNS
     */
    public String resolveUrlToIp(String url) throws Exception {
        try {
            InetAddress inetAddress = InetAddress.getByName(url);
            return inetAddress.getHostAddress();
        } catch (Exception e) {
            throw new Exception("Error al resolver la URL: " + e.getMessage());
        }
    }

    /**
     * Valida que una cadena sea una URL válida (simplificado).
     * @param url cadena a validar
     * @return true si es URL válida, false en otro caso
     */
    private boolean isValidUrl(String url) {
        String regex = "^(https?://)?([a-z0-9-]+\\.)+[a-z0-9]{2,4}(:[0-9]{1,5})?(\\/.*)?$";
        return url.matches(regex);
    }

    /**
     * Construye el comando nmap a ejecutar según tipo de escaneo.
     * @param scanType tipo de escaneo ("intermediate", "deep", otro)
     * @param target IP o dominio objetivo
     * @return comando nmap listo para ejecutar
     */
    private String buildCommand(String scanType, String target) {
        return switch (scanType) {
            case "intermediate" -> "nmap -sV -sC -sS " + target;
            case "deep" -> "nmap -sV -sC -T4 -A " + target;
            default -> "nmap -sV -sC " + target;
        };
    }

    /**
     * Obtiene la IP pública del servidor desde un servicio externo.
     * @return IP pública o "Desconocida" si falla
     */
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

    /**
     * Obtiene la ubicación geográfica aproximada del servidor usando un API externa.
     * @return ciudad y país separados por coma, o "Desconocida"
     */
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
                        ? jsonStr.replaceAll(".*\"city\":\"([^\"]+)\".*", "$1")
                        : "";
                String country = jsonStr.matches(".*\"country_name\":\"[^\"]+\".*")
                        ? jsonStr.replaceAll(".*\"country_name\":\"([^\"]+)\".*", "$1")
                        : "";

                String loc = (city + ", " + country).trim();
                return loc.isBlank() ? "Desconocida" : loc;
            }
        } catch (Exception e) {
            return "Desconocida";
        }
    }
}
