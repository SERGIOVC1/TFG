package com.tfg.tfg.persistance.repository;

import com.tfg.tfg.persistance.model.HeadersLogResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HeadersLogResultRepository extends JpaRepository<HeadersLogResult, Long> {
}
