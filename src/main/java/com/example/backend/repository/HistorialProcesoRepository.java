package com.example.backend.repository;

import com.example.backend.entity.HistorialProceso;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HistorialProcesoRepository extends JpaRepository<HistorialProceso, Long> {
    List<HistorialProceso> findByProcesoIdOrderByFechaDesc(Long procesoId);
}