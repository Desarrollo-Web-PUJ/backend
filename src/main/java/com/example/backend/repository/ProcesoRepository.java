package com.example.backend.repository;

import com.example.backend.entity.Proceso;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcesoRepository extends JpaRepository<Proceso, Long> {
}
