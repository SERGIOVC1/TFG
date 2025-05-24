package com.tfg.tfg.controllers;

import com.tfg.tfg.services.UserSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    @Autowired
    private UserSessionService userSessionService;

    // Recibe el userId y detalles de la acción
    @PostMapping("/log-action")
    public String logAction(
        @RequestHeader("X-User-Id") String userId,
        @RequestParam String tableName,
        @RequestParam String action,
        @RequestParam String details
    ) {
        userSessionService.registerAction(userId, tableName, action, details);
        return "Acción registrada con éxito para usuario: " + userId;
    }
}
