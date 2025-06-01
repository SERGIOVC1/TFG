package com.tfg.tfg.persistance.repository;

import com.tfg.tfg.persistance.model.HeadersLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HeadersLogRepository extends JpaRepository<HeadersLog, Long> {
    List<HeadersLog> findByUserId(String userId);
}
