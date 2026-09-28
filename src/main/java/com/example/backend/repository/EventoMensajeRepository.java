package com.example.backend.repository;

import com.example.backend.entity.EventoMensaje;
import com.example.backend.entity.TipoEventoMensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EventoMensajeRepository extends JpaRepository<EventoMensaje, Long> {

    List<EventoMensaje> findByProcesoIdAndActivoTrue(Long procesoId);

    Optional<EventoMensaje> findByIdAndProcesoIdAndActivoTrue(Long id, Long procesoId);

    // HU-25: para verificar que exista un catch con el mismo nombre
    List<EventoMensaje> findByProcesoIdAndNombreMensajeAndTipoInAndActivoTrue(
            Long procesoId,
            String nombreMensaje,
            List<TipoEventoMensaje> tipos
    );

    // HU-28: detectar ambigüedad nombre + clave de correlación
    List<EventoMensaje> findByProcesoIdAndNombreMensajeAndClaveCorrelacionAndActivoTrue(
            Long procesoId,
            String nombreMensaje,
            String claveCorrelacion
    );

    // HU-27: catch de inicio no puede tener arcos entrantes
    boolean existsByPoolIdAndTipoAndActivoTrue(Long poolId, TipoEventoMensaje tipo);

    boolean existsByPoolIdAndActivoTrue(Long poolId);

    boolean existsByLaneIdAndActivoTrue(Long laneId);

    @Query("""
            SELECT COUNT(e) > 0
            FROM EventoMensaje e
            WHERE e.proceso.id = :procesoId
            AND e.pool.id = :poolId
            AND e.tipo = :tipo
            AND LOWER(e.nombreMensaje) = LOWER(:nombreMensaje)
            AND COALESCE(e.claveCorrelacion, '') = COALESCE(:claveCorrelacion, '')
            AND e.activo = true
            AND (:eventoId IS NULL OR e.id <> :eventoId)
            """)
    boolean existsDuplicadoActivo(
            @Param("procesoId") Long procesoId,
            @Param("poolId") Long poolId,
            @Param("tipo") TipoEventoMensaje tipo,
            @Param("nombreMensaje") String nombreMensaje,
            @Param("claveCorrelacion") String claveCorrelacion,
            @Param("eventoId") Long eventoId
    );
}
