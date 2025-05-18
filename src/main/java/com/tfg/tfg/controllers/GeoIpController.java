package com.tfg.tfg.controllers;

import com.tfg.tfg.persistance.model.IpGeoLog;
import com.tfg.tfg.services.IpGeoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/geoip")
@CrossOrigin(origins = "*")
public class GeoIpController {

    @Autowired
    private IpGeoService ipGeoService;

    @PostMapping("/log")
    public ResponseEntity<?> saveGeoIpLog(@RequestBody IpGeoLog log) {
        ipGeoService.saveLog(log);
        return ResponseEntity.ok("Log de geolocalización guardado correctamente.");
    }
}
