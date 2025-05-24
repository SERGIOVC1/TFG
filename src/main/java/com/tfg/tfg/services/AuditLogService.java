package com.tfg.tfg.services;

import com.tfg.tfg.dto.AuditLogDto;
import com.tfg.tfg.persistance.model.AuditLog;
import com.tfg.tfg.persistance.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    // Obtener logs de auditoría de un usuario
    public List<AuditLogDto> getAuditLogsByUser(String userId) {
        List<AuditLog> logs = auditLogRepository.findByUserIdOrderByTimestampDesc(userId);

        return logs.stream().map(log -> new AuditLogDto(
                log.getAction(),
                log.getTableName(),
                log.getRecordId(),  
                log.getTimestamp(),
                log.getDetails()
        )).collect(Collectors.toList());
    }
}
