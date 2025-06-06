package com.tfg.tfg.controllers;

// Importaciones necesarias para manejar modelos, servicios y peticiones HTTP
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
@RequestMapping("/api/link")  // Define la ruta base para este controlador
@CrossOrigin("*")  // Permite solicitudes desde cualquier origen (CORS)
public class LinkController {

    @Autowired
    private LinkService linkService;  // Inyección automática del servicio de enlaces

    // Endpoint POST para generar un enlace corto a partir de una URL enviada en el cuerpo JSON
    @PostMapping
    public ResponseEntity<?> generateLink(@RequestBody Map<String, String> payload) {
        String originalUrl = payload.get("url");  // Obtiene la URL original enviada en el JSON

        // Validación básica: si la URL es nula o está vacía, devuelve error 400
        if (originalUrl == null || originalUrl.isBlank()) {
            return ResponseEntity.badRequest().body("URL no válida");
        }

        // Llama al servicio para crear un código corto para la URL
        String code = linkService.createShortCode(originalUrl);

        // Construye el enlace final que apunta al endpoint de redirección usando el código generado
        String finalLink = "http://localhost:8080/api/link/go/" + code;

        // Devuelve una respuesta 200 con el enlace corto y el código
        return ResponseEntity.ok(Map.of("shortened", finalLink, "code", code));
    }

    // Endpoint GET para redirigir a la URL original y registrar el acceso, recibe el código como parámetro en la URL
    @GetMapping("/go/{code}")
    public RedirectView redirectAndLog(@PathVariable String code, HttpServletRequest request) {
        // Usa el servicio para registrar el acceso y obtener la URL original asociada al código
        String original = linkService.trackAndRedirect(code, request);

        // Retorna una redirección HTTP hacia la URL original o a una URL por defecto si no existe
        return new RedirectView(original != null ? original : "https://example.com");
    }

    // Endpoint GET para obtener los logs de accesos asociados a un código de enlace corto
    @GetMapping("/logs/{code}")
    public ResponseEntity<List<LinkLog>> getLogs(@PathVariable String code) {
        // Llama al servicio para obtener la lista de logs del código proporcionado
        List<LinkLog> logs = linkService.getLogsForCode(code);

        // Devuelve la lista de logs con código 200 OK
        return ResponseEntity.ok(logs);
    }
}
