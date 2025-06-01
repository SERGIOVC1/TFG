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

import java.net.InetAddress;
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

    @PostMapping("/scan")
    public ResponseEntity<Map<String, String>> scanNetwork(
            @RequestBody Map<String, String> request,
            HttpServletRequest servletRequest) {

        String target = request.get("target");
        String scanType = request.get("scanType");
        String userId = request.get("userId");  // <-- Capturamos userId desde JSON

        if (target == null || target.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El campo 'target' es obligatorio."));
        }

        try {
            // Pasamos userId al service para guardar en log
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

    @PostMapping("/log")
    public ResponseEntity<?> saveScanLog(
            @RequestBody WebScannerLogRequest request,
            HttpServletRequest servletRequest) {
        try {
            String internalIp = servletRequest.getRemoteAddr();

            WebScannerLog log = new WebScannerLog();

            log.setUserId(request.getUserId());  // Guardamos userId en el log
            log.setIpAddress(request.getIpAddress());
            log.setInternalIpAddress(internalIp);
            log.setAction(request.getAction());
            log.setDetails(request.getDetails());
            log.setResult(request.getResult());
            log.setToolUsed(request.getToolUsed());

            if (request.getTimestamp() != null) {
                log.setTimestamp(java.time.Instant.ofEpochMilli(request.getTimestamp()));
            } else {
                log.setTimestamp(java.time.Instant.now());
            }

            log.setUserAgent(request.getUserAgent());
            log.setBot(request.isBot());
            log.setLocation(request.getLocation());

            webScannerLogRepository.save(log);

            return ResponseEntity.ok("Log guardado correctamente");

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al guardar log: " + e.getMessage());
        }
    }
}
