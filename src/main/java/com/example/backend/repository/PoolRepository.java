package com.example.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.entity.Pool;

public interface PoolRepository extends JpaRepository<Pool, Long> {

    List<Pool> findByProcesoIdAndActivoTrue(Long procesoId);

    Optional<Pool> findByIdAndProcesoId(Long id, Long procesoId);

    boolean existsByProcesoIdAndNombreIgnoreCaseAndActivoTrue(Long procesoId, String nombre);

    boolean existsByProcesoIdAndNombreIgnoreCaseAndIdNotAndActivoTrue(
            Long procesoId, String nombre, Long idActual);
}