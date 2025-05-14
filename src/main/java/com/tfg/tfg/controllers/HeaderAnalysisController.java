package com.tfg.tfg.controllers;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/security")
public class HeaderAnalysisController {

    @GetMapping("/headers")
    public ResponseEntity<Map<String, String>> analyzeHeaders(@RequestParam String url) {
        try {
            if (!url.startsWith("http")) {
                url = "https://" + url;
            }

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = restTemplate.headForHeaders(URI.create(url));

            Map<String, String> securityHeaders = new HashMap<>();
            String[] keys = {
                    "Content-Security-Policy",
                    "X-Content-Type-Options",
                    "Strict-Transport-Security",
                    "X-Frame-Options",
                    "Referrer-Policy",
                    "Permissions-Policy"
            };

            for (String key : keys) {
                String value = headers.getFirst(key);
                securityHeaders.put(key, value != null ? value : "❌ No establecido");
            }

            return ResponseEntity.ok(securityHeaders);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Map.of("error", "Error al analizar la URL: " + e.getMessage())
            );
        }
    }
}
