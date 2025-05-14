package com.tfg.tfg.services;

import com.tfg.tfg.persistance.model.HoneypotLog;
import com.tfg.tfg.persistance.repository.HoneypotLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HoneypotService {

    @Autowired
    private HoneypotLogRepository honeypotLogRepository;

    /**
     * Guarda un registro de cualquier herramienta (no solo honeypot)
     */
    public void logHoneypotAction(
            String ipAddress,
            String internalIpAddress,
            String action,
            String details,
            String result,
            String toolUsed,
            Long timestamp,
            String userAgent,
            boolean isBot,
            String location // ✅ nuevo parámetro
    ) {
        try {
            HoneypotLog honeypotLog = new HoneypotLog();
            honeypotLog.setIpAddress(ipAddress);
            honeypotLog.setInternalIpAddress(internalIpAddress);
            honeypotLog.setAction(action);
            honeypotLog.setDetails(details);
            honeypotLog.setResult(result);
            honeypotLog.setToolUsed(toolUsed);
            honeypotLog.setTimestamp(timestamp);
            honeypotLog.setUserAgent(userAgent);
            honeypotLog.setIsBot(isBot);
            honeypotLog.setLocation(location); // ✅ guardar ubicación

            honeypotLogRepository.save(honeypotLog);
            System.out.println("✅ Registro guardado correctamente");
        } catch (Exception e) {
            System.err.println("❌ Error al guardar log: " + e.getMessage());
        }
    }
}
