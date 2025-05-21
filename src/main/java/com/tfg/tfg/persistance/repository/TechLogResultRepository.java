package com.tfg.tfg.persistance.repository;

import com.tfg.tfg.persistance.model.TechLogResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TechLogResultRepository extends JpaRepository<TechLogResult, Long> {
}
