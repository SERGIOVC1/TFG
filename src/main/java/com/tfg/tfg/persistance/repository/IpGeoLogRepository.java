package com.tfg.tfg.persistance.repository;

import com.tfg.tfg.persistance.model.IpGeoLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IpGeoLogRepository extends JpaRepository<IpGeoLog, Long> {
}
