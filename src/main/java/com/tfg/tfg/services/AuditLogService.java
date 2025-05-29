package com.tfg.tfg.services;

import com.tfg.tfg.dto.AuditLogDto;
import com.tfg.tfg.persistance.model.AuditLog;
import com.tfg.tfg.persistance.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    // Obtener logs filtrados por userId
    public List<AuditLogDto> getAuditLogsByUser(String userId) {
        List<AuditLog> logs = auditLogRepository.findByUserIdOrderByTimestampDesc(userId);
        return mapToDto(logs);
    }

    // Obtener todos los logs (sin filtro)
    public List<AuditLogDto> getAllAuditLogs() {
        List<AuditLog> logs = auditLogRepository.findAll();
        return mapToDto(logs);
    }

    // Crear un nuevo log incluyendo userId
    public void createAuditLog(AuditLogDto dto) {
        AuditLog log = new AuditLog();
        log.setUserId(dto.getUserId());
        log.setAction(dto.getAction());
        log.setTableName(dto.getTableName());
        log.setRecordId(dto.getRecordId());
        log.setTimestamp(dto.getTimestamp() != null ? dto.getTimestamp() : Instant.now());
        log.setDetails(dto.getDetails());
        auditLogRepository.save(log);
    }

    // Método privado para mapear entity a DTO, incluyendo userId
    private List<AuditLogDto> mapToDto(List<AuditLog> logs) {
        return logs.stream().map(log -> new AuditLogDto(
                log.getUserId(),
                log.getAction(),
                log.getTableName(),
                log.getRecordId(),
                log.getTimestamp(),
                log.getDetails()
        )).collect(Collectors.toList());
    }
}
