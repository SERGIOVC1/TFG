package com.tfg.tfg.controllers;

import com.tfg.tfg.persistance.model.HeadersLog;
import com.tfg.tfg.persistance.repository.HeadersLogRepository;
import com.tfg.tfg.services.HeadersLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/security")
@CrossOrigin(origins = "*")
public class HeaderAnalysisController {

    private final HeadersLogService headersLogService;
    private final HeadersLogRepository headersLogRepository;

    @Autowired
    public HeaderAnalysisController(HeadersLogService headersLogService, HeadersLogRepository headersLogRepository) {
        this.headersLogService = headersLogService;
        this.headersLogRepository = headersLogRepository;
    }

    // Endpoint para analizar headers y registrar con userId opcional
    @GetMapping("/headers")
    public ResponseEntity<Map<String, String>> analyzeHeaders(
            @RequestParam String url,
            @RequestParam(required = false) String userId  // userId opcional
    ) {
        try {
            if (!url.startsWith("http")) {
                url = "https://" + url;
            }

            Map<String, String> securityHeaders = headersLogService.analyzeAndSave(url, userId);

            return ResponseEntity.ok(securityHeaders);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Error al analizar la URL: " + e.getMessage())
            );
        }
    }

    // Nuevo endpoint para obtener logs filtrados por userId
    @GetMapping("/logs")
    public ResponseEntity<List<HeadersLog>> getHeadersLogs(@RequestParam(required = false) String userId) {
        List<HeadersLog> logs;
        if (userId != null && !userId.isEmpty()) {
            logs = headersLogRepository.findByUserId(userId);
        } else {
            logs = headersLogRepository.findAll();
        }
        return ResponseEntity.ok(logs);
    }
}
