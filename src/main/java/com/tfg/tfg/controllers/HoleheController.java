package com.tfg.tfg.controllers;

// Importaciones necesarias para manejar la lógica, modelos y servicios
import com.tfg.tfg.persistance.model.HoleheLog;
import com.tfg.tfg.persistance.repository.HoleheLogRepository;
import com.tfg.tfg.services.HoleheService;
import com.tfg.tfg.dto.HoleheLogRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.InetAddress;
import java.time.Instant;

@RestController
@RequestMapping("/api/holehe")  // Define la ruta base para los endpoints de este controlador
@CrossOrigin(origins = "*")  // Permite solicitudes desde cualquier origen (CORS)
public class HoleheController {

    // Inyección de dependencias para el servicio y repositorio
    private final HoleheService holeheService;
    private final HoleheLogRepository holeheLogRepository;

    // Constructor para inyectar las dependencias necesarias
    public HoleheController(HoleheService holeheService, HoleheLogRepository holeheLogRepository) {
        this.holeheService = holeheService;
        this.holeheLogRepository = holeheLogRepository;
    }

    // Endpoint GET para ejecutar el servicio Holehe con un email recibido como parámetro
    @GetMapping
    public String runHolehe(@RequestParam String email) {
        return holeheService.runHolehe(email);  // Llama al servicio para procesar el email y devuelve el resultado
    }

    // Endpoint POST para guardar logs relacionados con Holehe
    @PostMapping("/log")
    public ResponseEntity<?> saveLog(@RequestBody HoleheLogRequest request) {
        try {
            // Obtiene la IP local del servidor donde corre la aplicación
            String internalIp = InetAddress.getLocalHost().getHostAddress();

            // Crea un nuevo objeto de tipo HoleheLog para guardar la información recibida
            HoleheLog log = new HoleheLog();
            log.setUserId(request.getUserId());  // Asigna el userId recibido en la petición
            log.setIpAddress(request.getIpAddress());  // IP externa
            log.setInternalIpAddress(internalIp);  // IP interna del servidor
            log.setAction(request.getAction());  // Acción que se registró
            log.setDetails(request.getDetails());  // Detalles adicionales
            log.setResult(request.getResult());  // Resultado de la acción
            log.setToolUsed(request.getToolUsed());  // Herramienta utilizada

            // Maneja la fecha/hora del log: si viene en la petición, la usa; sino usa el tiempo actual
            if (request.getTimestamp() != null) {
                log.setTimestamp(Instant.ofEpochMilli(request.getTimestamp()));
            } else {
                log.setTimestamp(Instant.now());
            }

            log.setUserAgent(request.getUserAgent());  // Información del navegador o cliente que hizo la petición
            log.setIsBot(request.isBot());  // Si la petición fue hecha por un bot o no
            log.setLocation(request.getLocation());  // Ubicación geográfica

            // Guarda el log en la base de datos usando el repositorio
            holeheLogRepository.save(log);

            // Retorna una respuesta HTTP 200 OK con mensaje de éxito
            return ResponseEntity.ok("Log Holehe guardado correctamente");

        } catch (Exception e) {
            e.printStackTrace();  // Muestra el error en consola para debug
            // Retorna un error 500 con el mensaje del problema
            return ResponseEntity.status(500).body("Error al registrar Holehe: " + e.getMessage());
        }
    }
}
