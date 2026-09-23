package com.example.backend.service;

import com.example.backend.dto.LaneDTO;
import com.example.backend.entity.Lane;
import com.example.backend.entity.Proceso;
import com.example.backend.repository.LaneRepository;
import com.example.backend.repository.ProcesoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LaneServiceImpl implements LaneService {

    private final LaneRepository laneRepository;
    private final ProcesoRepository procesoRepository;

    public LaneServiceImpl(LaneRepository laneRepository, ProcesoRepository procesoRepository) {
        this.laneRepository = laneRepository;
        this.procesoRepository = procesoRepository;
    }

    @Override
    public List<LaneDTO> listarPorProceso(Long procesoId) {
        return laneRepository.findByProcesoId(procesoId).stream()
                .map(l -> new LaneDTO(l.getId(), l.getNombre()))
                .collect(Collectors.toList());
    }

    @Override
    public LaneDTO crearLane(Long procesoId, String nombre) {
        Proceso proceso = procesoRepository.findById(procesoId)
                .orElseThrow(() -> new IllegalArgumentException("El proceso no existe"));
        Lane lane = laneRepository.save(new Lane(nombre, proceso));
        return new LaneDTO(lane.getId(), lane.getNombre());
    }
}