package com.tfg.tfg.controllers;

import com.tfg.tfg.services.TechnologyScannerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/tech-scan") // Ruta base para este controlador
@CrossOrigin(origins = "*")       // Permite solicitudes desde cualquier origen (CORS)
public class TechnologyScannerController {

    private final TechnologyScannerService scannerService;

    // Constructor para inyección de dependencia del servicio
    public TechnologyScannerController(TechnologyScannerService scannerService) {
        this.scannerService = scannerService;
    }

    // Endpoint GET para escanear tecnologías usadas en un dominio web
    @GetMapping
    public ResponseEntity<Map<String, String>> scan(
            @RequestParam String domain,           // Dominio a analizar (obligatorio)
            @RequestParam(required = false) String userId) {  // Opcional: ID del usuario que hace la petición

        // Llama al servicio para analizar el sitio web y obtener tecnologías detectadas
        Map<String, String> result = scannerService.analyzeWebsite(domain, userId);

        // Retorna el resultado del análisis con código HTTP 200 OK
        return ResponseEntity.ok(result);
    }
}
