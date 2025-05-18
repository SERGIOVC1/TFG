package com.tfg.tfg.controllers;

import com.tfg.tfg.persistance.model.Ipresolver;
import com.tfg.tfg.persistance.repository.IpresolverLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ipresolver")
public class IpresolverController {

    @Autowired
    private IpresolverLogRepository ipresolverLogRepository;

    @PostMapping("/log")
    public ResponseEntity<?> log(@RequestBody Ipresolver log) {
        try {
            System.out.println("📥 [DEBUG] Petición recibida:");
            System.out.println("➡ IP externa: " + log.getIpAddress());
            System.out.println("➡ IP interna: " + log.getInternalIpAddress());
            System.out.println("➡ Acción: " + log.getAction());
            System.out.println("➡ Detalles: " + log.getDetails());
            System.out.println("➡ Resultado: " + log.getResult());
            System.out.println("➡ User Agent: " + log.getUserAgent());
            System.out.println("➡ Is Bot: " + log.getIsBot());
            System.out.println("➡ Ubicación: " + log.getLocation());
            System.out.println("➡ Timestamp: " + log.getTimestamp());

            ipresolverLogRepository.save(log);
            System.out.println("✅ Log guardado correctamente en la base de datos");

            return ResponseEntity.ok("Log guardado correctamente");
        } catch (Exception e) {
            System.err.println("❌ Error al guardar log: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error al guardar el log");
        }
    }
}
