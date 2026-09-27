package com.example.backend.service;

import com.example.backend.dto.ArcoRequestDTO;
import com.example.backend.dto.ArcoResponseDTO;
import com.example.backend.dto.EliminacionArcoResponseDTO;
import com.example.backend.entity.RolUsuario;

public interface ArcoService {

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