package com.tfg.tfg.persistance.repository;

import com.tfg.tfg.persistance.model.IpGeoLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IpGeoLogRepository extends JpaRepository<IpGeoLog, Long> {

    // Query para filtrar por userId ordenados por timestamp descendente
    List<IpGeoLog> findByUserIdOrderByTimestampDesc(String userId);
}
