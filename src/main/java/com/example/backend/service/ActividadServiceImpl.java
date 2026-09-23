package com.example.backend.service;

import com.example.backend.dto.ActividadDTO;
import com.example.backend.dto.ActividadRequestDTO;
import com.example.backend.entity.*;
import com.example.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ActividadServiceImpl implements ActividadService {

    private final ActividadRepository actividadRepository;
    private final LaneRepository laneRepository;
    private final ProcesoRepository procesoRepository;
    private final HistorialProcesoRepository historialProcesoRepository;

    public ActividadServiceImpl(ActividadRepository actividadRepository,
                                 LaneRepository laneRepository,
                                 ProcesoRepository procesoRepository,
                                 HistorialProcesoRepository historialProcesoRepository) {
        this.actividadRepository = actividadRepository;
        this.laneRepository = laneRepository;
        this.procesoRepository = procesoRepository;
        this.historialProcesoRepository = historialProcesoRepository;
    }

    @Override
    @Transactional
    public ActividadDTO crearActividad(Long procesoId, ActividadRequestDTO request) {
        Proceso proceso = procesoRepository.findById(procesoId)
                .orElseThrow(() -> new IllegalArgumentException("El proceso no existe"));

        if (request.getNombre() == null || request.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre de la actividad es obligatorio");
        }

        if (actividadRepository.existsByNombreAndProcesoId(request.getNombre(), procesoId)) {
            throw new IllegalArgumentException("Ya existe una actividad con ese nombre en este proceso");
        }

        if (request.getLaneId() == null) {
            throw new IllegalArgumentException("Debe seleccionar la lane responsable");
        }

        Lane lane = laneRepository.findById(request.getLaneId())
                .orElseThrow(() -> new IllegalArgumentException("La lane no existe"));

        if (!lane.getProceso().getId().equals(procesoId)) {
            throw new IllegalArgumentException("La lane seleccionada no pertenece a este proceso");
        }

        Actividad actividad = new Actividad();
        actividad.setNombre(request.getNombre());
        actividad.setTipo(request.getTipo());
        actividad.setProceso(proceso);
        actividad.setLane(lane);
        actividad.setPosicionX(request.getPosicionX());
        actividad.setPosicionY(request.getPosicionY());

        actividad = actividadRepository.save(actividad);

        HistorialProceso historial = new HistorialProceso(
                proceso,
                "Se creó la actividad '" + actividad.getNombre() + "' en la lane '" + lane.getNombre() + "'",
                LocalDateTime.now()
        );
        historialProcesoRepository.save(historial);

        return toDTO(actividad);
    }

    @Override
    public List<ActividadDTO> listarPorProceso(Long procesoId) {
        return actividadRepository.findByProcesoId(procesoId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    private ActividadDTO toDTO(Actividad a) {
        ActividadDTO dto = new ActividadDTO();
        dto.setId(a.getId());
        dto.setNombre(a.getNombre());
        dto.setTipo(a.getTipo());
        dto.setProcesoId(a.getProceso().getId());
        dto.setLaneId(a.getLane().getId());
        dto.setLaneNombre(a.getLane().getNombre());
        dto.setPosicionX(a.getPosicionX());
        dto.setPosicionY(a.getPosicionY());
        return dto;
    }
}