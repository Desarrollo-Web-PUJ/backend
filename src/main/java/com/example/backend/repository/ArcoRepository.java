package com.example.backend.repository;

import com.example.backend.entity.Arco;
import com.example.backend.entity.TipoNodo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ArcoRepository extends JpaRepository<Arco, Long> {

    List<Arco> findByProcesoId(Long procesoId);

    Optional<Arco> findByIdAndProcesoId(
            Long id,
            Long procesoId
    );

    boolean existsByProcesoIdAndTipoOrigenAndOrigenIdAndTipoDestinoAndDestinoId(
            Long procesoId,
            TipoNodo tipoOrigen,
            Long origenId,
            TipoNodo tipoDestino,
            Long destinoId
    );

    boolean existsByProcesoIdAndTipoOrigenAndOrigenIdAndTipoDestinoAndDestinoIdAndIdNot(
            Long procesoId,
            TipoNodo tipoOrigen,
            Long origenId,
            TipoNodo tipoDestino,
            Long destinoId,
            Long id
    );

    long countByProcesoIdAndTipoOrigenAndOrigenId(
            Long procesoId,
            TipoNodo tipoOrigen,
            Long origenId
    );

    long countByProcesoIdAndTipoDestinoAndDestinoId(
            Long procesoId,
            TipoNodo tipoDestino,
            Long destinoId
    );
}