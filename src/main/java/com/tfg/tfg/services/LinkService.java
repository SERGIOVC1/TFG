package com.tfg.tfg.services;

import com.tfg.tfg.persistance.model.LinkLog;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.util.*;

@Service  // Marca esta clase como servicio gestionado por Spring
public class LinkService {

    // Mapa que relaciona código corto con URL original
    private final Map<String, String> urlMap = new HashMap<>();

    // Mapa que guarda logs de visitas para cada código corto (máx 5 logs por código)
    private final Map<String, Deque<LinkLog>> logsMap = new HashMap<>();

    /**
     * Crea un código corto para una URL original y la guarda en urlMap.
     * Inicializa también un registro vacío de logs para ese código.
     * @param originalUrl URL que se quiere acortar
     * @return código corto generado (6 caracteres)
     */
    public String createShortCode(String originalUrl) {
        String code = UUID.randomUUID().toString().substring(0, 6); // código único corto
        urlMap.put(code, originalUrl);  // Asocia código con URL
        logsMap.put(code, new LinkedList<>());  // Inicializa lista de logs vacía para este código
        return code;
    }

    /**
     * Registra una visita a la URL acortada y devuelve la URL original para redireccionar.
     * Extrae información de la petición (IP cliente, user agent, IP servidor, etc).
     * Limita los logs a 5 por código, eliminando los más antiguos.
     * @param code código corto recibido
     * @param request petición HTTP para obtener info visitante
     * @return URL original para redireccionar o null si no existe
     */
    public String trackAndRedirect(String code, HttpServletRequest request) {
        String originalUrl = urlMap.get(code);
        if (originalUrl != null) {
            // Obtiene IP cliente real revisando varios headers HTTP comunes
            String clientIp = getClientIp(request);
            // Obtiene User-Agent del navegador o cliente
            String userAgent = request.getHeader("User-Agent");

            // Obtiene IP pública y local del servidor (donde corre la app)
            String serverPublicIp = getPublicIp();
            String serverLocalIp = getLocalIp();

            // Crea un objeto LinkLog con la info de la visita
            LinkLog log = new LinkLog();
            log.setClientIp(clientIp);
            log.setUserAgent(userAgent);
            log.setCode(code);
            log.setServerPublicIp(serverPublicIp);
            log.setServerLocalIp(serverLocalIp);
            log.setTimestamp(System.currentTimeMillis());

            // Obtiene la cola de logs para este código, la crea si no existe
            Deque<LinkLog> logs = logsMap.get(code);
            if (logs == null) {
                logs = new LinkedList<>();
                logsMap.put(code, logs);
            }
            logs.addLast(log);  // Añade el nuevo log al final
            if (logs.size() > 5) {
                logs.removeFirst();  // Mantiene sólo los últimos 5 logs
            }

            // Imprime en consola información de la visita detectada
            System.out.println("📡 Visita detectada:");
            System.out.println("IP cliente: " + clientIp);
            System.out.println("User-Agent: " + userAgent);
            System.out.println("IP pública servidor: " + serverPublicIp);
            System.out.println("IP local servidor: " + serverLocalIp);
            System.out.println("Redireccionando a: " + originalUrl);
        }
        return originalUrl;  // Retorna URL para la redirección
    }

    /**
     * Devuelve la lista de logs de visitas para un código dado.
     * Si no hay logs, devuelve lista vacía.
     * @param code código corto a consultar
     * @return lista con logs de visitas
     */
    public List<LinkLog> getLogsForCode(String code) {
        Deque<LinkLog> logs = logsMap.get(code);
        return logs == null ? Collections.emptyList() : new ArrayList<>(logs);
    }

    /**
     * Método para obtener la IP real del cliente desde la petición HTTP,
     * comprobando múltiples cabeceras comunes para proxies y balanceadores.
     * @param request petición HTTP
     * @return IP cliente detectada
     */
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
                // Si hay múltiples IPs, se toma la primera (cliente original)
                return ipList.split(",")[0].trim();
            }
        }

        // Si no se detecta cabecera especial, toma la IP remota directa
        String ip = request.getRemoteAddr();
        // Si es localhost en IPv6, lo transforma a IPv4 localhost
        if ("0:0:0:0:0:0:0:1".equals(ip)) {
            ip = "127.0.0.1";
        }
        return ip;
    }

    /**
     * Obtiene la IP pública del servidor consultando un servicio externo.
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
     * Obtiene la IP local del servidor donde corre la aplicación.
     * @return IP local o "Desconocida" si falla
     */
    private String getLocalIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "Desconocida";
        }
    }
}
