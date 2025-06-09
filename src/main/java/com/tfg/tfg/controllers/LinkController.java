package com.tfg.tfg.controllers;

import com.tfg.tfg.persistance.model.LinkLog;
import com.tfg.tfg.services.LinkService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/link")
@CrossOrigin("*")
public class LinkController {

    @Autowired
    private LinkService linkService;

    // POST: Genera un enlace corto (con dominio fijo en Render)
    @PostMapping
    public ResponseEntity<?> generateLink(@RequestBody Map<String, String> payload) {
        String originalUrl = payload.get("url");

        if (originalUrl == null || originalUrl.isBlank()) {
            return ResponseEntity.badRequest().body("URL no válida");
        }

        String code = linkService.createShortCode(originalUrl);

        // 🔗 Dominio fijo (Render)
        String finalLink = "https://tfg-backend-wfvn.onrender.com/api/link/go/" + code;

        return ResponseEntity.ok(Map.of("shortened", finalLink, "code", code));
    }

    // GET: Redirige a la URL original y registra el acceso
    @GetMapping("/go/{code}")
    public RedirectView redirectAndLog(@PathVariable String code, HttpServletRequest request) {
        String original = linkService.trackAndRedirect(code, request);
        return new RedirectView(original != null ? original : "https://example.com");
    }

    // GET: Devuelve los logs de visitas al enlace corto
    @GetMapping("/logs/{code}")
    public ResponseEntity<List<LinkLog>> getLogs(@PathVariable String code) {
        return ResponseEntity.ok(linkService.getLogsForCode(code));
    }
}
