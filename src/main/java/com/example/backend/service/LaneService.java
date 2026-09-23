package com.example.backend.service;

import com.example.backend.dto.LaneDTO;
import java.util.List;

public interface LaneService {
    List<LaneDTO> listarPorProceso(Long procesoId);
    LaneDTO crearLane(Long procesoId, String nombre);
}