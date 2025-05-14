package com.tfg.tfg.controllers;

import com.tfg.tfg.services.WebDirectoryScannerService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/webscan")
@CrossOrigin("*")
public class WebScannerController {

    private final WebDirectoryScannerService webDirectoryScannerService;

    public WebScannerController(WebDirectoryScannerService webDirectoryScannerService) {
        this.webDirectoryScannerService = webDirectoryScannerService;
    }

    @GetMapping(value = "/directories", produces = "text/event-stream") // Cambiado de /subdomains a /directories
    public SseEmitter scanDirectories(@RequestParam String target) {
        return webDirectoryScannerService.scanDirectories(target); // Cambiado a scanDirectories()
    }
}
