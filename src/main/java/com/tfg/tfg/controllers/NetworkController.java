package com.tfg.tfg.controllers;

import com.tfg.tfg.dto.WebScannerLogRequest;
import com.tfg.tfg.persistance.model.WebScannerLog;
import com.tfg.tfg.persistance.repository.WebScannerLogRepository;
import com.tfg.tfg.services.NetworkScannerService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.InetAddress;
import java.util.Map;

@RestController
@RequestMapping("/api/network") // Ruta base para este controlador
@CrossOrigin(origins = "*")   // Permite acceso desde cualquier origen (CORS)
public class NetworkController {

    private final NetworkScannerService networkScannerService;
    private final WebScannerLogRepository webScannerLogRepository;

    // Inyección de dependencias vía constructor
    @Autowired
    public NetworkController(NetworkScannerService networkScannerService,
                             WebScannerLogRepository webScannerLogRepository) {
        this.networkScannerService = networkScannerService;
        this.webScannerLogRepository = webScannerLogRepository;
    }

    // Endpoint POST para escanear una red o IP según parámetros recibidos
    @PostMapping("/scan")
    public ResponseEntity<Map<String, String>> scanNetwork(
            @RequestBody Map<String, String> request, // Parámetros JSON recibidos
            HttpServletRequest servletRequest) {     // Información HTTP de la petición

        String target = request.get("target");      // IP o dominio a escanear
        String scanType = request.get("scanType");  // Tipo de escaneo (ej: ping, puertos)
        String userId = request.get("userId");      // ID del usuario para registro en logs

        // Validación: target es obligatorio
        if (target == null || target.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El campo 'target' es obligatorio."));
        }

        try {
            // Ejecuta el escaneo usando el servicio, pasando userId para guardar en logs
            String scanResult = networkScannerService.scanNetwork(target, scanType, servletRequest, userId);

            // Devuelve resultado exitoso con información del escaneo
            return ResponseEntity.ok(Map.of(
                    "target", target,
                    "scanType", scanType,
                    "result", scanResult
            ));

        } catch (Exception e) {
            // Si hay error, responde con error 500 y mensaje descriptivo
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al realizar el escaneo: " + e.getMessage()));
        }
    }

    // Endpoint POST para guardar logs del escaneo realizado
    @PostMapping("/log")
    public ResponseEntity<?> saveScanLog(
            @RequestBody WebScannerLogRequest request,   // Datos recibidos para guardar log
            HttpServletRequest servletRequest) {        // Info HTTP (para obtener IP interna)

        try {
            // Obtiene la IP interna del cliente que hizo la petición (del servlet)
            String internalIp = servletRequest.getRemoteAddr();

            // Crea nuevo objeto log para persistir
            WebScannerLog log = new WebScannerLog();

            // Setea todos los datos en el objeto log
            log.setUserId(request.getUserId());          // ID usuario
            log.setIpAddress(request.getIpAddress());    // IP pública o objetivo escaneado
            log.setInternalIpAddress(internalIp);        // IP interna desde donde se hizo la petición
            log.setAction(request.getAction());           // Acción realizada
            log.setDetails(request.getDetails());         // Detalles extra
            log.setResult(request.getResult());           // Resultado del escaneo
            log.setToolUsed(request.getToolUsed());       // Herramienta utilizada

            // Si se recibe timestamp, usa ese, sino usa el momento actual
            if (request.getTimestamp() != null) {
                log.setTimestamp(java.time.Instant.ofEpochMilli(request.getTimestamp()));
            } else {
                log.setTimestamp(java.time.Instant.now());
            }

            log.setUserAgent(request.getUserAgent());   // Info del agente usuario
            log.setBot(request.isBot());                 // Si es bot o no
            log.setLocation(request.getLocation());     // Ubicación geográfica

            // Guarda el log en la base de datos
            webScannerLogRepository.save(log);

            // Retorna mensaje de éxito
            return ResponseEntity.ok("Log guardado correctamente");

        } catch (Exception e) {
            // En caso de error devuelve 500 y mensaje con excepción
            return ResponseEntity.status(500).body("Error al guardar log: " + e.getMessage());
        }
    }
}
