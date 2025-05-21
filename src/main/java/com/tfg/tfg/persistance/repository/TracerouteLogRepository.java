package com.tfg.tfg.persistance.repository;

import com.tfg.tfg.persistance.model.TracerouteLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TracerouteLogRepository extends JpaRepository<TracerouteLog, Long> {
}
