package com.example.backend.repository;

import com.example.backend.entity.Actividad;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ActividadRepository extends JpaRepository<Actividad, Long> {
    boolean existsByNombreAndProcesoId(String nombre, Long procesoId);
    List<Actividad> findByProcesoId(Long procesoId);
}