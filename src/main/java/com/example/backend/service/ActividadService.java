package com.example.backend.service;

import com.example.backend.dto.ActividadDTO;
import com.example.backend.dto.ActividadRequestDTO;
import com.example.backend.dto.EliminacionActividadResponseDTO;
import com.example.backend.entity.RolUsuario;

import java.util.List;

public interface ActividadService {

    ActividadDTO crearActividad(
            Long procesoId,
            ActividadRequestDTO request,
            Long empresaId,
            RolUsuario rol
    );

    List<ActividadDTO> listarPorProceso(
            Long procesoId,
            Long empresaId
    );

    // HU-09: Editar actividad
    ActividadDTO editarActividad(
            Long procesoId,
            Long actividadId,
            ActividadRequestDTO request,
            Long empresaId,
            RolUsuario rol
    );

    // HU-10: Eliminar actividad
    EliminacionActividadResponseDTO eliminarActividad(
            Long procesoId,
            Long actividadId,
            Long empresaId,
            RolUsuario rol
    );
}
