package com.tfg.tfg.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class IPController {

    @GetMapping("/honeypot/internal-ip")
    public String getInternalIp(HttpServletRequest request) {
        // Primero verifica si la cabecera X-Forwarded-For está presente (si es accesible desde un proxy)
        String internalIp = request.getHeader("X-FORWARDED-FOR");
        
        if (internalIp == null || internalIp.isEmpty()) {
            // Si no está disponible, obtiene la IP remota (la IP local del cliente)
            internalIp = request.getRemoteAddr();
        }
        
        return internalIp; // Devolver la IP interna
    }
}
