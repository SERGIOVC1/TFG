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
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
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
        String ip = target;

        if (isValidUrl(target)) {
            ip = resolveUrlToIp(target);
        }

        // ⚠️ Fuerza siempre el tipo "basic" para evitar errores por falta de privilegios
        String result = forwardToMicroservice(ip, "basic");

        WebScannerLog log = new WebScannerLog();
        log.setUserId(userId);
        log.setIpAddress(getPublicIp());
        log.setInternalIpAddress(InetAddress.getLocalHost().getHostAddress());
        log.setAction("Network Scan");
        log.setDetails(target);
        log.setResult(result);
        log.setToolUsed("network_scan");
        log.setTimestamp(Instant.now());
        log.setUserAgent(request.getHeader("User-Agent"));
        log.setBot(false);
        log.setLocation(getLocation());

        WebScannerLog savedLog = logRepository.save(log);

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

    public String forwardToMicroservice(String target, String scanType) throws Exception {
        // 🛠️ Actualiza esta URL con la última que te dé ngrok
        String microserviceUrl = "https://848b-84-125-184-18.ngrok-free.app/scan/nmap";

        String jsonInputString = String.format("{\"target\": \"%s\", \"scanType\": \"%s\"}", target, scanType);

        URL url = new URL(microserviceUrl);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);

        try (var os = con.getOutputStream()) {
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        StringBuilder response = new StringBuilder();
        try (var br = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line).append("\n");
            }
        }

        con.disconnect();
        return response.toString();
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
