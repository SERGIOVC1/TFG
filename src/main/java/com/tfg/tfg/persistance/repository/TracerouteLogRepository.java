package com.tfg.tfg.persistance.repository;

import com.tfg.tfg.persistance.model.TracerouteLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TracerouteLogRepository extends JpaRepository<TracerouteLog, Long> {
    List<TracerouteLog> findByUserId(String userId);
}
