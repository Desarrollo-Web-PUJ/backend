package com.example.backend.service;

import com.example.backend.dto.ActividadDTO;
import com.example.backend.dto.ActividadRequestDTO;
import java.util.List;

public interface ActividadService {
    ActividadDTO crearActividad(Long procesoId, ActividadRequestDTO request);
    List<ActividadDTO> listarPorProceso(Long procesoId);
}