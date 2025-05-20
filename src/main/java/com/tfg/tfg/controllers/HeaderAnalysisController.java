package com.tfg.tfg.controllers;

import com.tfg.tfg.services.HeadersLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/security")
@CrossOrigin(origins = "*")
public class HeaderAnalysisController {

    private final HeadersLogService headersLogService;

    @Autowired
    public HeaderAnalysisController(HeadersLogService headersLogService) {
        this.headersLogService = headersLogService;
    }

    @GetMapping("/headers")
    public ResponseEntity<Map<String, String>> analyzeHeaders(@RequestParam String url) {
        try {
            if (!url.startsWith("http")) {
                url = "https://" + url;
            }

            // Llama al método analyzeAndSave para analizar y guardar el resultado
            Map<String, String> securityHeaders = headersLogService.analyzeAndSave(url);

            return ResponseEntity.ok(securityHeaders);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Error al analizar la URL: " + e.getMessage())
            );
        }
    }
}
