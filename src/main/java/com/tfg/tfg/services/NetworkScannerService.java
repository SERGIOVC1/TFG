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

    public String scanNetwork(String target, String scanType, HttpServletRequest request, String userId) throws Exception {
        String resolvedTarget = isValidUrl(target) ? resolveUrlToIp(target) : target;

        String command = buildCommand(scanType, resolvedTarget);
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

        WebScannerLog log = new WebScannerLog();
        log.setUserId(userId);
        log.setIpAddress(getPublicIp());
        log.setInternalIpAddress(InetAddress.getLocalHost().getHostAddress());
        log.setAction("Network Scan");
        log.setDetails(target);
        log.setResult(result);
        log.setToolUsed("nmap");
        log.setTimestamp(Instant.now());
        log.setUserAgent(request.getHeader("User-Agent"));
        log.setBot(false);
        log.setLocation(getLocation());

        WebScannerLog savedLog = logRepository.save(log);

        List<WebScannerResult> parsedResults = parseScanResults(result, savedLog);
        resultRepository.saveAll(parsedResults);

        return result;
    }

    private List<WebScannerResult> parseScanResults(String scanOutput, WebScannerLog log) {
        List<WebScannerResult> results = new ArrayList<>();
        String[] lines = scanOutput.split("\n");
        Pattern pattern = Pattern.compile("(\\d+/tcp)\\s+(open|closed|filtered)\\s+(\\S+)");

        for (String line : lines) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                WebScannerResult entry = new WebScannerResult();
                entry.setLog(log);
                entry.setPort(matcher.group(1));
                entry.setState(matcher.group(2));
                entry.setService(matcher.group(3));
                results.add(entry);
            }
        }
        return results;
    }

    private boolean isValidUrl(String url) {
        String regex = "^(https?://)?([a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}(:\\d+)?(/.*)?$";
        return url.matches(regex);
    }

    private String resolveUrlToIp(String url) throws Exception {
        try {
            return InetAddress.getByName(url).getHostAddress();
        } catch (Exception e) {
            throw new Exception("Error al resolver la URL: " + e.getMessage());
        }
    }

    private String buildCommand(String scanType, String target) {
        return switch (scanType) {
            case "intermediate" -> "/usr/bin/nmap -sV -sC -sS " + target;
            case "deep" -> "/usr/bin/nmap -sV -sC -T4 -A " + target;
            default -> "/usr/bin/nmap -sV -sC " + target;
        };
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
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
                StringBuilder json = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
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
