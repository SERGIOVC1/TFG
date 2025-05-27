package com.tfg.tfg.controllers;

import com.tfg.tfg.dto.WebScannerLogRequest;
import com.tfg.tfg.persistance.model.WebScannerLog;
import com.tfg.tfg.persistance.model.WebScannerResult;
import com.tfg.tfg.persistance.repository.WebScannerLogRepository;
import com.tfg.tfg.persistance.repository.WebScannerResultRepository;
import com.tfg.tfg.services.NetworkScannerService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/network")
@CrossOrigin(origins = "*")
public class NetworkController {

    private final NetworkScannerService networkScannerService;
    private final WebScannerLogRepository webScannerLogRepository;
    private final WebScannerResultRepository webScannerResultRepository;

    @Autowired
    public NetworkController(NetworkScannerService networkScannerService,
                             WebScannerLogRepository webScannerLogRepository,
                             WebScannerResultRepository webScannerResultRepository) {
        this.networkScannerService = networkScannerService;
        this.webScannerLogRepository = webScannerLogRepository;
        this.webScannerResultRepository = webScannerResultRepository;
    }

    // 🔍 Escaneo de red
    @PostMapping("/scan")
    public ResponseEntity<Map<String, String>> scanNetwork(
            @RequestBody Map<String, String> request,
            HttpServletRequest servletRequest) {

        String target = request.get("target");
        String scanType = request.get("scanType");

        if (target == null || target.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El campo 'target' es obligatorio."));
        }

        try {
            String scanResult = networkScannerService.scanNetwork(target, scanType, servletRequest);

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

    // 🌐 Resolver dominio a IP
    @GetMapping("/resolve-ip")
    public ResponseEntity<Map<String, String>> resolveIp(@RequestParam String domain) {
        try {
            String ip = networkScannerService.resolveUrlToIp(domain);
            return ResponseEntity.ok(Map.of("ip", ip));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al resolver el dominio: " + e.getMessage()));
        }
    }

    // 🧾 Guardar log desde frontend manual (opcional)
    @PostMapping("/log")
    public ResponseEntity<?> saveScanLog(
            @RequestBody WebScannerLogRequest request,
            HttpServletRequest servletRequest) {
        try {
            String internalIp = servletRequest.getRemoteAddr();

            WebScannerLog log = new WebScannerLog();
            // Dentro del método saveScanLog

            log.setIpAddress(request.getIpAddress());
            log.setInternalIpAddress(internalIp);
            log.setAction(request.getAction());
            log.setDetails(request.getDetails());
            log.setResult(request.getResult());
            log.setToolUsed(request.getToolUsed());

            // Convertir Long timestamp a Instant
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

    // 📊 Obtener los puertos por ID de log (extra)
    @GetMapping("/logs/{logId}/ports")
    public ResponseEntity<?> getPortsByLog(@PathVariable Long logId) {
        try {
            List<WebScannerResult> ports = webScannerResultRepository.findByLogId(logId);
            return ResponseEntity.ok(ports);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al obtener puertos: " + e.getMessage());
        }
    }
}
