package com.tfg.tfg.persistance.repository;

import com.tfg.tfg.persistance.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    // Método para buscar logs filtrando por usuario y ordenando por fecha descendente
    List<AuditLog> findByUserIdOrderByTimestampDesc(String userId);
}
