package com.tfg.tfg.controllers;

import com.tfg.tfg.dto.WebScannerLogRequest;
import com.tfg.tfg.persistance.model.WebScannerLog;
import com.tfg.tfg.persistance.repository.WebScannerLogRepository;
import com.tfg.tfg.services.NetworkScannerService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/network")
@CrossOrigin(origins = "*")
public class NetworkController {

    private final NetworkScannerService networkScannerService;
    private final WebScannerLogRepository webScannerLogRepository;

    @Autowired
    public NetworkController(NetworkScannerService networkScannerService,
                             WebScannerLogRepository webScannerLogRepository) {
        this.networkScannerService = networkScannerService;
        this.webScannerLogRepository = webScannerLogRepository;
    }

    /**
     * Endpoint principal para realizar escaneos de red (usando Nmap).
     */
    @PostMapping("/scan")
    public ResponseEntity<Map<String, String>> scanNetwork(
            @RequestBody Map<String, String> request,
            HttpServletRequest servletRequest) {

        String target = request.get("target");
        String scanType = request.getOrDefault("scanType", "basic");
        String userId = request.getOrDefault("userId", "anonymous");

        if (target == null || target.isBlank()) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "El campo 'target' es obligatorio.")
            );
        }

        try {
            String scanResult = networkScannerService.scanNetwork(target, scanType, servletRequest, userId);

            return ResponseEntity.ok(Map.of(
                    "target", target,
                    "scanType", scanType,
                    "result", scanResult
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al realizar el escaneo: " + e.getMessage()));
        }
    }

    /**
     * Endpoint alternativo para guardar logs manualmente (si se desea).
     */
    @PostMapping("/log")
    public ResponseEntity<?> saveScanLog(
            @RequestBody WebScannerLogRequest request,
            HttpServletRequest servletRequest) {

        try {
            String internalIp = servletRequest.getRemoteAddr();

            WebScannerLog log = new WebScannerLog();
            log.setUserId(request.getUserId());
            log.setIpAddress(request.getIpAddress());
            log.setInternalIpAddress(internalIp);
            log.setAction(request.getAction());
            log.setDetails(request.getDetails());
            log.setResult(request.getResult());
            log.setToolUsed(request.getToolUsed());
            log.setTimestamp(request.getTimestamp() != null ?
                    Instant.ofEpochMilli(request.getTimestamp()) : Instant.now());
            log.setUserAgent(request.getUserAgent());
            log.setBot(request.isBot());
            log.setLocation(request.getLocation());

            webScannerLogRepository.save(log);

            return ResponseEntity.ok("Log guardado correctamente");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al guardar log: " + e.getMessage());
        }
    }
}
