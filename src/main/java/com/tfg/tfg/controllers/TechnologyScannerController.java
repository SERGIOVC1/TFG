package com.tfg.tfg.controllers;

import com.tfg.tfg.services.TechnologyScannerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/tech-scan")
@CrossOrigin(origins = "*") // puedes restringir si quieres
public class TechnologyScannerController {

    private final TechnologyScannerService scannerService;

    public TechnologyScannerController(TechnologyScannerService scannerService) {
        this.scannerService = scannerService;
    }

    @GetMapping
    public ResponseEntity<Map<String, String>> scan(@RequestParam String domain) {
        Map<String, String> result = scannerService.analyzeWebsite(domain);
        return ResponseEntity.ok(result);
    }
}
