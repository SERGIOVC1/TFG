package com.tfg.tfg.controllers;

import com.tfg.tfg.dto.WhoisLogRequest;
import com.tfg.tfg.persistance.model.WhoisLog;
import com.tfg.tfg.persistance.repository.WhoisLogRepository;
import com.tfg.tfg.services.WhoisService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.InetAddress;
import java.time.Instant;

@RestController
@RequestMapping("/api/whois")             // Ruta base para este controlador
@CrossOrigin(origins = "*")              // Permite peticiones CORS desde cualquier origen
public class WhoisController {

    @Autowired
    private WhoisService whoisService;   // Servicio para hacer consultas WHOIS

    @Autowired
    private WhoisLogRepository whoisLogRepository;  // Repositorio para guardar logs WHOIS en BD

    // Endpoint GET para realizar una consulta WHOIS para un dominio dado
    @GetMapping
    public String getWhois(@RequestParam String domain) {
        return whoisService.lookup(domain);  // Devuelve la respuesta WHOIS como String
    }

    // Endpoint POST para guardar un log de una consulta WHOIS
    @PostMapping("/log")
    public ResponseEntity<?> saveLog(@RequestBody WhoisLogRequest request) {
        try {
            // Obtener la IP interna de la máquina donde corre el backend
            String internalIp = InetAddress.getLocalHost().getHostAddress();

            WhoisLog log = new WhoisLog();     // Crear un nuevo objeto log para guardar info
            log.setIpAddress(request.getIpAddress());          // IP pública desde la que se hizo la consulta
            log.setInternalIpAddress(internalIp);              // IP interna del servidor backend
            log.setDomain(request.getDomain());                // Dominio consultado
            log.setWhoisResult(request.getWhoisResult());      // Resultado de la consulta WHOIS
            log.setToolUsed(request.getToolUsed());            // Herramienta usada (ej. "whois")

            // Si el timestamp está presente, convertirlo a Instant, si no usar la fecha actual
            if (request.getTimestamp() != null) {
                log.setTimestamp(Instant.ofEpochMilli(request.getTimestamp()));
            } else {
                log.setTimestamp(Instant.now());
            }

            log.setUserAgent(request.getUserAgent());          // User agent del cliente que hizo la consulta
            log.setBot(request.isBot());                        // Indica si la consulta fue hecha por un bot
            log.setLocation(request.getLocation());            // Ubicación geográfica aproximada del usuario
            log.setAction(request.getAction());                 // Acción realizada (puede ser opcional o descriptiva)
            log.setDetails(request.getDetails());               // Detalles adicionales o notas del log

            whoisLogRepository.save(log);                       // Guardar el log en la base de datos
            return ResponseEntity.ok("Log WHOIS guardado correctamente"); // Respuesta exitosa

        } catch (Exception e) {
            e.printStackTrace();
            // En caso de error devolver un status 500 con mensaje de error
            return ResponseEntity.status(500).body("Error al registrar WHOIS: " + e.getMessage());
        }
    }

}
