package com.tfg.tfg.controllers;

import com.tfg.tfg.persistance.model.HoleheLog;
import com.tfg.tfg.persistance.repository.HoleheLogRepository;
import com.tfg.tfg.services.HoleheService;
import com.tfg.tfg.dto.HoleheLogRequest;  // Te recomiendo crear este DTO similar al WhoisLogRequest
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

    @GetMapping
    public String runHolehe(@RequestParam String email) {
        return holeheService.runHolehe(email);
    }

    @PostMapping("/log")
    public ResponseEntity<?> saveLog(@RequestBody HoleheLogRequest request) {
        try {
            String internalIp = InetAddress.getLocalHost().getHostAddress();

            HoleheLog log = new HoleheLog();
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
