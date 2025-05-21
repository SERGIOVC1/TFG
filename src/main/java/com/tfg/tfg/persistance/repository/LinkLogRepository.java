package com.tfg.tfg.persistance.repository;

import com.tfg.tfg.persistance.model.LinkLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LinkLogRepository extends JpaRepository<LinkLog, Long> {
    List<LinkLog> findByCodeOrderByTimestampDesc(String code);
}
