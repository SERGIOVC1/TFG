// WebScannerController.java
package com.tfg.tfg.controllers;

import com.tfg.tfg.services.WebDirectoryScannerService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/webscan")   // Define la ruta base para este controlador
@CrossOrigin(origins = "*")       // Permite peticiones CORS desde cualquier origen
public class WebScannerController {

    private final WebDirectoryScannerService webDirectoryScannerService;

    // Constructor para inyección del servicio
    public WebScannerController(WebDirectoryScannerService webDirectoryScannerService) {
        this.webDirectoryScannerService = webDirectoryScannerService;
    }

    /**
     * Endpoint para iniciar el escaneo de directorios en un dominio.
     * Devuelve resultados progresivos usando Server-Sent Events (SSE).
     *
     * @param target dominio o IP a escanear
     * @param userId ID del usuario que ejecuta el escaneo (opcional)
     * @return SseEmitter para enviar resultados en tiempo real al cliente
     */
    @GetMapping(value = "/directories", produces = "text/event-stream")
    public SseEmitter scanDirectories(
            @RequestParam String target,
            @RequestParam(required = false) String userId) {
        return webDirectoryScannerService.scanDirectories(target, userId);
    }
}
