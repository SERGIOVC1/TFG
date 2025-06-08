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
import java.util.HashMap;
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
        String userId = request.get("userId");

        if (target == null || target.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El campo 'target' es obligatorio."));
        }

        try {
            // Redirige la petición al microservicio Node.js vía HTTP
            String scanResult = networkScannerService.forwardToMicroservice(target, scanType);

            // Log opcional si quieres guardar desde aquí (alternativa a /log)
            WebScannerLog log = new WebScannerLog();
            log.setUserId(userId);
            log.setIpAddress(servletRequest.getRemoteAddr());
            log.setInternalIpAddress(InetAddress.getLocalHost().getHostAddress());
            log.setAction("Network Scan");
            log.setDetails(target);
            log.setResult(scanResult);
            log.setToolUsed("microservice_nmap");
            log.setTimestamp(java.time.Instant.now());
            log.setUserAgent(servletRequest.getHeader("User-Agent"));
            log.setBot(false);
            log.setLocation("Desconocida");
            webScannerLogRepository.save(log);

            Map<String, String> response = new HashMap<>();
            response.put("target", target);
            response.put("scanType", scanType);
            response.put("result", scanResult);

            return ResponseEntity.ok(response);

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
            log.setUserId(request.getUserId());
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
