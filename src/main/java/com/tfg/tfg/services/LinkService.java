package com.tfg.tfg.services;

import com.tfg.tfg.persistance.model.LinkLog;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class LinkService {

    private final Map<String, String> urlMap = new HashMap<>();
    private final Map<String, Deque<LinkLog>> logsMap = new HashMap<>(); // ⬅️ Mantenemos máximo 5 logs por enlace

    public String createShortCode(String originalUrl) {
        String code = UUID.randomUUID().toString().substring(0, 6);
        urlMap.put(code, originalUrl);
        logsMap.put(code, new LinkedList<>()); // Inicializar cola de logs vacía
        return code;
    }

    public String trackAndRedirect(String code, HttpServletRequest request) {
        String originalUrl = urlMap.get(code);
        if (originalUrl != null) {
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null) ip = request.getRemoteAddr();

            String userAgent = request.getHeader("User-Agent");

            // Crear nuevo log
            LinkLog log = new LinkLog(ip, userAgent, code);

            // Añadir a la cola y mantener solo los últimos 5 registros
            Deque<LinkLog> logs = logsMap.get(code);
            if (logs == null) {
                logs = new LinkedList<>();
                logsMap.put(code, logs);
            }
            logs.addLast(log);
            if (logs.size() > 5) {
                logs.removeFirst(); // Eliminar el más antiguo
            }

            System.out.println("📡 Visita detectada:");
            System.out.println("IP: " + ip);
            System.out.println("User-Agent: " + userAgent);
            System.out.println("Redireccionando a: " + originalUrl);
        }
        return originalUrl;
    }

    // 🆕 Para el frontend: obtener logs por código
    public List<LinkLog> getLogsForCode(String code) {
        Deque<LinkLog> logs = logsMap.get(code);
        return logs == null ? Collections.emptyList() : new ArrayList<>(logs);
    }
}
