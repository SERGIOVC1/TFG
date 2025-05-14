package com.tfg.tfg.persistance.repository;

import com.tfg.tfg.persistance.model.Scan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScanRepository extends JpaRepository<Scan, Long> {
}
