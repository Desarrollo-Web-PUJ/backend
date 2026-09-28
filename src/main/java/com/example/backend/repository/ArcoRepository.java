package com.example.backend.repository;

import com.example.backend.entity.Arco;
import com.example.backend.entity.TipoNodo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ArcoRepository extends JpaRepository<Arco, Long> {

    List<Arco> findByProcesoIdAndActivoTrue(Long procesoId);

    Optional<Arco> findByIdAndProcesoIdAndActivoTrue(
            Long id,
            Long procesoId
    );

    Optional<Arco> findFirstByProcesoIdAndTipoOrigenAndOrigenIdAndTipoDestinoAndDestinoIdAndActivoFalseOrderByIdDesc(
            Long procesoId,
            TipoNodo tipoOrigen,
            Long origenId,
            TipoNodo tipoDestino,
            Long destinoId
    );

    boolean existsByProcesoIdAndTipoOrigenAndOrigenIdAndTipoDestinoAndDestinoIdAndActivoTrue(
            Long procesoId,
            TipoNodo tipoOrigen,
            Long origenId,
            TipoNodo tipoDestino,
            Long destinoId
    );

    boolean existsByProcesoIdAndTipoOrigenAndOrigenIdAndTipoDestinoAndDestinoIdAndActivoTrueAndIdNot(
            Long procesoId,
            TipoNodo tipoOrigen,
            Long origenId,
            TipoNodo tipoDestino,
            Long destinoId,
            Long id
    );

    long countByProcesoIdAndTipoOrigenAndOrigenIdAndActivoTrue(
            Long procesoId,
            TipoNodo tipoOrigen,
            Long origenId
    );

    long countByProcesoIdAndTipoDestinoAndDestinoIdAndActivoTrue(
            Long procesoId,
            TipoNodo tipoDestino,
            Long destinoId
    );
}
