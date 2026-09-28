package com.example.backend.repository;

import com.example.backend.entity.FlujoMensaje;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FlujoMensajeRepository extends JpaRepository<FlujoMensaje, Long> {

    List<FlujoMensaje> findByProcesoIdAndActivoTrue(Long procesoId);

    Optional<FlujoMensaje> findByIdAndProcesoIdAndActivoTrue(Long id, Long procesoId);

    long countByOrigenIdAndActivoTrue(Long origenId);

    long countByDestinoIdAndActivoTrue(Long destinoId);
}