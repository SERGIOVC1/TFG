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

    // Endpoint para obtener logs filtrados por usuario
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AuditLogDto>> getAuditLogsForUser(@PathVariable String userId) {
        List<AuditLogDto> logs = auditLogService.getAuditLogsByUser(userId);
        return ResponseEntity.ok(logs);
    }
}
