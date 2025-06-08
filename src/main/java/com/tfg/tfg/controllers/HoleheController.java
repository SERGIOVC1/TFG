package com.tfg.tfg.controllers;

import com.tfg.tfg.dto.HoleheLogRequest;
import com.tfg.tfg.persistance.model.HoleheLog;
import com.tfg.tfg.persistance.repository.HoleheLogRepository;
import com.tfg.tfg.services.HoleheService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.InetAddress;
import java.time.Instant;

@RestController
@RequestMapping("/api/holehe")
@CrossOrigin(origins = "*")
public class HoleheController {

    private final HoleheService holeheService;
    private final HoleheLogRepository holeheLogRepository;

    public HoleheController(HoleheService holeheService, HoleheLogRepository holeheLogRepository) {
        this.holeheService = holeheService;
        this.holeheLogRepository = holeheLogRepository;
    }

    // ✅ Nuevo endpoint POST que llama al microservicio y registra automáticamente el log
    @PostMapping("/scan")
    public ResponseEntity<?> scanEmail(
            @RequestBody HoleheLogRequest request,
            HttpServletRequest servletRequest) {

        try {
            // Validación básica
            if (request.getDetails() == null || request.getDetails().isBlank()) {
                return ResponseEntity.badRequest().body("Email no proporcionado");
            }

            String result = holeheService.runHolehe(
                    request.getDetails(),
                    servletRequest,
                    request.getUserId() != null ? request.getUserId() : "desconocido"
            );

            return ResponseEntity.ok().body(result);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error al ejecutar Holehe: " + e.getMessage());
        }
    }

    // Endpoint manual para guardar log si lo deseas por separado (opcional)
    @PostMapping("/log")
    public ResponseEntity<?> saveLog(@RequestBody HoleheLogRequest request) {
        try {
            String internalIp = InetAddress.getLocalHost().getHostAddress();

            HoleheLog log = new HoleheLog();
            log.setUserId(request.getUserId());
            log.setIpAddress(request.getIpAddress());
            log.setInternalIpAddress(internalIp);
            log.setAction(request.getAction());
            log.setDetails(request.getDetails());
            log.setResult(request.getResult());
            log.setToolUsed(request.getToolUsed());

            if (request.getTimestamp() != null) {
                log.setTimestamp(Instant.ofEpochMilli(request.getTimestamp()));
            } else {
                log.setTimestamp(Instant.now());
            }

            log.setUserAgent(request.getUserAgent());
            log.setIsBot(request.isBot());
            log.setLocation(request.getLocation());

            holeheLogRepository.save(log);
            return ResponseEntity.ok("Log Holehe guardado correctamente");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error al registrar Holehe: " + e.getMessage());
        }
    }
}
