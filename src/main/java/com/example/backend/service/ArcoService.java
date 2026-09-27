package com.example.backend.service;

import com.example.backend.dto.ArcoRequestDTO;
import com.example.backend.dto.ArcoResponseDTO;
import com.example.backend.dto.EliminacionArcoResponseDTO;
import com.example.backend.entity.RolUsuario;

import java.util.List;

public interface ArcoService {

    List<ArcoResponseDTO> listarPorProceso(
            Long procesoId,
            Long empresaId
    );

    ArcoResponseDTO crearArco(
            Long procesoId,
            ArcoRequestDTO request,
            Long empresaId,
            RolUsuario rol
    );

    ArcoResponseDTO editarArco(
            Long procesoId,
            Long arcoId,
            ArcoRequestDTO request,
            Long empresaId,
            RolUsuario rol
    );

    EliminacionArcoResponseDTO eliminarArco(
            Long procesoId,
            Long arcoId,
            Long empresaId,
            RolUsuario rol
    );
}