package com.tfg.tfg.persistance.repository;

import com.tfg.tfg.persistance.model.WebScannerResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WebScannerResultRepository extends JpaRepository<WebScannerResult, Long> {
    List<WebScannerResult> findByLogId(Long logId);
}
