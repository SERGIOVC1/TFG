package com.tfg.tfg.persistance.repository;

import com.tfg.tfg.persistance.model.Ipresolver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IpresolverLogRepository extends JpaRepository<Ipresolver, Long> {
}
