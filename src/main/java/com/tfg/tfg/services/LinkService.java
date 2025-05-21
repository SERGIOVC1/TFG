package com.tfg.tfg.services;

import com.tfg.tfg.persistance.model.LinkLog;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.util.*;

@Service
public class LinkService {

    private final Map<String, String> urlMap = new HashMap<>();
    private final Map<String, Deque<LinkLog>> logsMap = new HashMap<>(); // Máximo 5 logs por código

    public String createShortCode(String originalUrl) {
        String code = UUID.randomUUID().toString().substring(0, 6);
        urlMap.put(code, originalUrl);
        logsMap.put(code, new LinkedList<>()); // Inicializar cola de logs vacía
        return code;
    }

    public String trackAndRedirect(String code, HttpServletRequest request) {
        String originalUrl = urlMap.get(code);
        if (originalUrl != null) {
            String clientIp = getClientIp(request);
            String userAgent = request.getHeader("User-Agent");

            String serverPublicIp = getPublicIp();
            String serverLocalIp = getLocalIp();

            LinkLog log = new LinkLog();
            log.setClientIp(clientIp);
            log.setUserAgent(userAgent);
            log.setCode(code);
            log.setServerPublicIp(serverPublicIp);
            log.setServerLocalIp(serverLocalIp);
            log.setTimestamp(System.currentTimeMillis());

            Deque<LinkLog> logs = logsMap.get(code);
            if (logs == null) {
                logs = new LinkedList<>();
                logsMap.put(code, logs);
            }
            logs.addLast(log);
            if (logs.size() > 5) {
                logs.removeFirst();
            }

            System.out.println("📡 Visita detectada:");
            System.out.println("IP cliente: " + clientIp);
            System.out.println("User-Agent: " + userAgent);
            System.out.println("IP pública servidor: " + serverPublicIp);
            System.out.println("IP local servidor: " + serverLocalIp);
            System.out.println("Redireccionando a: " + originalUrl);
        }
        return originalUrl;
    }

    public List<LinkLog> getLogsForCode(String code) {
        Deque<LinkLog> logs = logsMap.get(code);
        return logs == null ? Collections.emptyList() : new ArrayList<>(logs);
    }

    private String getClientIp(HttpServletRequest request) {
        String[] headersToCheck = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
        };

        for (String header : headersToCheck) {
            String ipList = request.getHeader(header);
            if (ipList != null && ipList.length() != 0 && !"unknown".equalsIgnoreCase(ipList)) {
                return ipList.split(",")[0].trim();
            }
        }

        String ip = request.getRemoteAddr();
        if ("0:0:0:0:0:0:0:1".equals(ip)) {
            ip = "127.0.0.1";
        }
        return ip;
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

    private String getLocalIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "Desconocida";
        }
    }
}
