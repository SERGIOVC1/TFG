package com.tfg.tfg.controllers;

import com.tfg.tfg.persistance.model.IpGeoLog;
import com.tfg.tfg.services.IpGeoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/geoip")
@CrossOrigin(origins = "*")
public class GeoIpController {

    @Autowired
    private IpGeoService ipGeoService;

    // Guardar log GeoIP
    @PostMapping("/log")
    public ResponseEntity<?> saveGeoIpLog(@RequestBody IpGeoLog log) {
        try {
            ipGeoService.saveLog(log);
            return ResponseEntity.ok("Log de geolocalización guardado correctamente.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error al guardar log GeoIP: " + e.getMessage());
        }
    }

    // Nuevo endpoint para obtener logs filtrados por userId
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<IpGeoLog>> getLogsByUser(@PathVariable String userId) {
        List<IpGeoLog> logs = ipGeoService.getLogsByUserId(userId);
        return ResponseEntity.ok(logs);
    }
}
