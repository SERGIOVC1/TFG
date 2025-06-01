package com.tfg.tfg.controllers;

import com.tfg.tfg.persistance.model.Ipresolver;
import com.tfg.tfg.persistance.repository.IpresolverLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.InetAddress;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ipresolver")
@CrossOrigin(origins = "*")
public class IpresolverController {

    @Autowired
    private IpresolverLogRepository ipresolverLogRepository;

    // Endpoint para resolver IP de un dominio
    @GetMapping("/resolve-ip")
    public ResponseEntity<Map<String, String>> resolveIp(@RequestParam String domain) {
        try {
            InetAddress inetAddress = InetAddress.getByName(domain);
            String ip = inetAddress.getHostAddress();
            Map<String, String> response = new HashMap<>();
            response.put("ip", ip);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "No se pudo resolver la IP para el dominio: " + domain));
        }
    }

    // Endpoint para guardar logs de IP resolver
    @PostMapping("/log")
    public ResponseEntity<?> log(@RequestBody Ipresolver log, HttpServletRequest servletRequest) {
        try {
            // Guardar IP interna desde la petición si no está seteada
            if (log.getInternalIpAddress() == null || log.getInternalIpAddress().isEmpty()) {
                log.setInternalIpAddress(servletRequest.getRemoteAddr());
            }
            // Timestamp si no está seteado
            if (log.getTimestamp() == null) {
                log.setTimestamp(OffsetDateTime.now());
            }

            ipresolverLogRepository.save(log);
            return ResponseEntity.ok("Log guardado correctamente");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error al guardar el log");
        }
    }
}
