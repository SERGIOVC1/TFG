// WebScannerController.java
package com.tfg.tfg.controllers;

import com.tfg.tfg.services.WebDirectoryScannerService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/webscan")   // Define la ruta base para este controlador
@CrossOrigin("*")               // Permite peticiones CORS desde cualquier origen
public class WebScannerController {

    private final WebDirectoryScannerService webDirectoryScannerService;

    // Constructor para inyección del servicio que realizará el escaneo de directorios
    public WebScannerController(WebDirectoryScannerService webDirectoryScannerService) {
        this.webDirectoryScannerService = webDirectoryScannerService;
    }

    // Endpoint GET para escanear directorios en un objetivo dado
    // Produce 'text/event-stream', que es formato para eventos enviados en tiempo real (SSE)
    @GetMapping(value = "/directories", produces = "text/event-stream")
    public SseEmitter scanDirectories(
            @RequestParam String target,            // Dirección o dominio objetivo a escanear (requerido)
            @RequestParam(required = false) String userId) {  // ID opcional del usuario que solicita el escaneo
        // Llama al servicio que realiza el escaneo y devuelve un SseEmitter para enviar resultados progresivos
        return webDirectoryScannerService.scanDirectories(target, userId);
    }
}
