package com.tfg.tfg.controllers;

import com.tfg.tfg.services.NetworkScannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/network")
public class NetworkController {

    private final NetworkScannerService networkScannerService;

    @Autowired
    public NetworkController(NetworkScannerService networkScannerService) {
        this.networkScannerService = networkScannerService;
    }

    // Endpoint para realizar el escaneo de red
    @PostMapping("/scan")
    public ResponseEntity<Map<String, String>> scanNetwork(@RequestBody Map<String, String> request) {
        String target = request.get("target");
        String scanType = request.get("scanType");

        // Verificar que el 'target' esté presente
        if (target == null || target.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El campo 'target' es obligatorio."));
        }

        try {
            // Llamamos al servicio para realizar el escaneo
            String scanResult = networkScannerService.scanNetwork(target, scanType);

            // Crear un mapa para devolver como respuesta
            Map<String, String> response = Map.of(
                "target", target,
                "scanType", scanType,
                "result", scanResult
            );

            return ResponseEntity.ok(response);  // Devolver el resultado del escaneo

        } catch (Exception e) {
            // Capturamos cualquier excepción y la devolvemos con un código 500
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al realizar el escaneo: " + e.getMessage()));
        }
    }

    // Endpoint para resolver el dominio a IP
    @GetMapping("/resolve-ip")
    public ResponseEntity<Map<String, String>> resolveIp(@RequestParam String domain) {
        try {
            // Llamamos al servicio para resolver el dominio a IP
            String ip = networkScannerService.resolveUrlToIp(domain);

            // Crear un mapa con la IP resuelta
            return ResponseEntity.ok(Map.of("ip", ip));
        } catch (Exception e) {
            // Si hay un error, respondemos con un código 500 y un mensaje de error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al resolver el dominio: " + e.getMessage()));
        }
    }
}
