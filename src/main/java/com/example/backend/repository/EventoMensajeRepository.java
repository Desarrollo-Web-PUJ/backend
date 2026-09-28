package com.example.backend.repository;

import com.example.backend.entity.EventoMensaje;
import com.example.backend.entity.TipoEventoMensaje;
import org.springframework.data.jpa.repository.JpaRepository;

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
}