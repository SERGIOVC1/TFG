package com.tfg.tfg.persistance.repository;

import com.tfg.tfg.persistance.model.TechLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TechLogRepository extends JpaRepository<TechLog, Long> {
}
