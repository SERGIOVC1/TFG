package com.tfg.tfg.controllers;

import com.tfg.tfg.services.FtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ftp")  // Ruta base para todas las peticiones de este controlador
public class FtpController {

    @Autowired
    private FtpService ftpService;  // Servicio que maneja la lógica para analizar FTP

    // Endpoint GET para hacer un escaneo/análisis FTP del target especificado
    @GetMapping("/scan")
    public List<String> scanFtp(@RequestParam String target) {
        // Llama al servicio que analiza FTP y devuelve una lista de resultados (ej. archivos/directorios)
        return ftpService.analyzeFtp(target);
    }
}
