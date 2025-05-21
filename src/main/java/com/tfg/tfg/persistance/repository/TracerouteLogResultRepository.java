package com.tfg.tfg.persistance.repository;

import com.tfg.tfg.persistance.model.TracerouteLogResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TracerouteLogResultRepository extends JpaRepository<TracerouteLogResult, Long> {
}
