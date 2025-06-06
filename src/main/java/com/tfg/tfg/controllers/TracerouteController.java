package com.tfg.tfg.controllers;

// package com.tfg.tfg.controllers;

import com.tfg.tfg.persistance.model.TracerouteLog;
import com.tfg.tfg.services.TracerouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/traceroute")  // Ruta base para el controlador traceroute
@CrossOrigin(origins = "*")         // Permite solicitudes desde cualquier origen (CORS)
public class TracerouteController {

    @Autowired
    private TracerouteService tracerouteService;  // Servicio para ejecutar traceroute y gestionar logs

    // Endpoint GET para ejecutar traceroute a un destino dado
    @GetMapping
    public List<String> trace(
            @RequestParam String target,            // Dirección IP o dominio destino a trazar (obligatorio)
            @RequestParam(required = false) String userId  // ID de usuario opcional para registro o seguimiento
    ) {
        // Ejecuta traceroute usando el servicio y devuelve la lista de saltos (hosts intermedios)
        return tracerouteService.executeTraceroute(target, userId);
    }

    // Endpoint GET para obtener logs de traceroute, opcionalmente filtrados por userId
    @GetMapping("/logs")
    public List<TracerouteLog> getLogs(@RequestParam(required = false) String userId) {
        // Recupera logs de traceroute del servicio según userId (si se provee)
        return tracerouteService.getLogsByUserId(userId);
    }
}
