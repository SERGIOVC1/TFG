// WebScannerController.java
package com.tfg.tfg.controllers;

import com.tfg.tfg.services.WebDirectoryScannerService;
import jakarta.servlet.http.HttpServletRequest;
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

    @GetMapping(value = "/directories", produces = "text/event-stream")
    public SseEmitter scanDirectories(
            @RequestParam String target,
            @RequestParam(required = false) String userId) {
        return webDirectoryScannerService.scanDirectories(target, userId);
    }
}
