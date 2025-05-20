package com.tfg.tfg.persistance.repository;

import com.tfg.tfg.persistance.model.WebScannerLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WebScannerLogRepository extends JpaRepository<WebScannerLog, Long> {
}
