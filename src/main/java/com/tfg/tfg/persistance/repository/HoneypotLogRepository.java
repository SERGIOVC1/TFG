package com.tfg.tfg.persistance.repository;

import com.tfg.tfg.persistance.model.HoneypotLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HoneypotLogRepository extends JpaRepository<HoneypotLog, Long> {
}
