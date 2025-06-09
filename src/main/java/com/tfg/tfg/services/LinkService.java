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

    // Almacena el código corto y su URL original asociada
    private final Map<String, String> urlMap = new HashMap<>();

    // Almacena los logs de cada código (máximo 5 entradas por código)
    private final Map<String, Deque<LinkLog>> logsMap = new HashMap<>();

    /**
     * Crea un código único para una URL original y lo almacena.
     * @param originalUrl URL que se quiere acortar
     * @return código generado de 6 caracteres
     */
    public String createShortCode(String originalUrl) {
        String code = UUID.randomUUID().toString().substring(0, 6);
        urlMap.put(code, originalUrl);
        logsMap.put(code, new LinkedList<>());
        return code;
    }

    /**
     * Registra una visita a un enlace y devuelve la URL original asociada.
     * @param code código corto recibido
     * @param request solicitud HTTP del visitante
     * @return la URL original o null si no existe
     */
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

            Deque<LinkLog> logs = logsMap.computeIfAbsent(code, k -> new LinkedList<>());
            logs.addLast(log);
            if (logs.size() > 5) {
                logs.removeFirst(); // Mantener solo los últimos 5
            }

            System.out.println("📡 Visita registrada:");
            System.out.println("  IP Cliente: " + clientIp);
            System.out.println("  User-Agent: " + userAgent);
            System.out.println("  IP Pública Servidor: " + serverPublicIp);
            System.out.println("  IP Local Servidor: " + serverLocalIp);
            System.out.println("  Redirigiendo a: " + originalUrl);
        }

        return originalUrl;
    }

    /**
     * Devuelve los logs para un código de enlace.
     * @param code código del enlace
     * @return lista de logs o vacía
     */
    public List<LinkLog> getLogsForCode(String code) {
        Deque<LinkLog> logs = logsMap.get(code);
        return logs == null ? Collections.emptyList() : new ArrayList<>(logs);
    }

    /**
     * Intenta detectar la IP real del cliente considerando cabeceras comunes.
     * @param request la petición HTTP
     * @return IP cliente
     */
    private String getClientIp(HttpServletRequest request) {
        String[] headers = {
            "X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR", "HTTP_X_FORWARDED", "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP", "HTTP_FORWARDED_FOR", "HTTP_FORWARDED",
            "HTTP_VIA", "REMOTE_ADDR"
        };

        for (String header : headers) {
            String ipList = request.getHeader(header);
            if (ipList != null && !ipList.isBlank() && !"unknown".equalsIgnoreCase(ipList)) {
                return ipList.split(",")[0].trim();
            }
        }

        String ip = request.getRemoteAddr();
        return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : ip;
    }

    /**
     * Consulta la IP pública usando un servicio externo.
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
     * Obtiene la IP local de la máquina que ejecuta el backend.
     */
    private String getLocalIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "Desconocida";
        }
    }
}
