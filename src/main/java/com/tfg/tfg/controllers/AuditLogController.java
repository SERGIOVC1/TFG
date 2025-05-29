package com.tfg.tfg.controllers;

import com.tfg.tfg.dto.AuditLogDto;
import com.tfg.tfg.services.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
@CrossOrigin(origins = "*")
public class AuditLogController {

    @Autowired
    private AuditLogService auditLogService;

    // Obtener logs de TODOS los usuarios (sin filtro)
    @GetMapping("/all")
    public ResponseEntity<List<AuditLogDto>> getAllAuditLogs() {
        List<AuditLogDto> logs = auditLogService.getAllAuditLogs();
        return ResponseEntity.ok(logs);
    }

    // Obtener logs filtrados por userId
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AuditLogDto>> getAuditLogsForUser(@PathVariable String userId) {
        List<AuditLogDto> logs = auditLogService.getAuditLogsByUser(userId);
        return ResponseEntity.ok(logs);
    }

    // Crear un nuevo log de auditoría
    @PostMapping
    public ResponseEntity<Void> createAuditLog(@RequestBody AuditLogDto auditLogDto) {
        auditLogService.createAuditLog(auditLogDto);
        return ResponseEntity.ok().build();
    }
}
