package com.tfg.tfg.persistance.repository;

import com.tfg.tfg.persistance.model.WhoisLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WhoisLogRepository extends JpaRepository<WhoisLog, Long> {
}
