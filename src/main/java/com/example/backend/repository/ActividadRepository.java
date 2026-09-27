package com.example.backend.repository;

import com.example.backend.entity.Actividad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ActividadRepository
        extends JpaRepository<Actividad, Long> {

    boolean existsByNombreAndProcesoId(
            String nombre,
            Long procesoId
    );

    boolean existsByNombreAndProcesoIdAndIdNot(
            String nombre,
            Long procesoId,
            Long id
    );

    List<Actividad> findByProcesoIdAndActivoTrue(
            Long procesoId
    );

    Optional<Actividad> findByIdAndProcesoIdAndActivoTrue(
            Long id,
            Long procesoId
    );
}