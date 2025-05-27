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

@Service
public class NetworkScannerService {

    @Autowired
    private WebScannerLogRepository logRepository;

    @Autowired
    private WebScannerResultRepository resultRepository;

    public String scanNetwork(String target, String scanType, HttpServletRequest request) throws Exception {
        String ip = target;

        if (isValidUrl(target)) {
            ip = resolveUrlToIp(target);
        }

        // Construimos el comando y agregamos opción para excluir código 301 en Gobuster si usas Gobuster
        // Si usas nmap solo, esta opción no aplica.
        // Para ejemplo con nmap:
        String command = buildCommand(scanType, ip);

        Process process = Runtime.getRuntime().exec(command);

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        process.waitFor();
        String result = output.toString();

        // Guardar log en base de datos
        WebScannerLog log = new WebScannerLog();
        log.setIpAddress(getPublicIp());
        log.setInternalIpAddress(InetAddress.getLocalHost().getHostAddress());
        log.setAction("Network Scan");
        log.setDetails(target);
        log.setResult(result);
        log.setToolUsed("network_scan");
        log.setTimestamp(Instant.now());  // Asegúrate que WebScannerLog tenga Instant para timestamp
        log.setUserAgent(request.getHeader("User-Agent"));
        log.setBot(false);
        log.setLocation(getLocation());

        WebScannerLog savedLog = logRepository.save(log);

        // Parsear puertos y guardar resultados (solo si usas nmap)
        List<WebScannerResult> parsedResults = new ArrayList<>();

        for (String line : result.split("\n")) {
            Matcher matcher = Pattern.compile("(\\d+/tcp)\\s+(\\w+)\\s+(\\S+)").matcher(line);
            if (matcher.find()) {
                WebScannerResult entry = new WebScannerResult();
                entry.setLog(savedLog);
                entry.setPort(matcher.group(1));
                entry.setState(matcher.group(2));
                entry.setService(matcher.group(3));
                parsedResults.add(entry);
            }
        }

        resultRepository.saveAll(parsedResults);

        return result;
    }

    public String resolveUrlToIp(String url) throws Exception {
        try {
            InetAddress inetAddress = InetAddress.getByName(url);
            return inetAddress.getHostAddress();
        } catch (Exception e) {
            throw new Exception("Error al resolver la URL: " + e.getMessage());
        }
    }

    private boolean isValidUrl(String url) {
        String regex = "^(https?://)?([a-z0-9-]+\\.)+[a-z0-9]{2,4}(:[0-9]{1,5})?(\\/.*)?$";
        return url.matches(regex);
    }

    private String buildCommand(String scanType, String target) {
        // Ejemplo usando nmap (modifica si usas gobuster o similar)
        return switch (scanType) {
            case "intermediate" -> "nmap -sV -sC -sS " + target;
            case "deep" -> "nmap -sV -sC -T4 -A " + target;
            default -> "nmap -sV -sC " + target;
        };
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
