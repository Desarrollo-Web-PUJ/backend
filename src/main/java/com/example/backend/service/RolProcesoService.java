package com.example.backend.service;

import com.example.backend.dto.EliminacionRolProcesoResponseDTO;
import com.example.backend.dto.LaneDTO;
import com.example.backend.dto.RolProcesoListItemDTO;
import com.example.backend.dto.RolProcesoRequestDTO;
import com.example.backend.dto.RolProcesoResponseDTO;
import com.example.backend.entity.RolUsuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RolProcesoService {

    // HU-17: Crear rol de proceso
    RolProcesoResponseDTO crear(
            RolProcesoRequestDTO request,
            Long empresaId,
            RolUsuario rol);

    // HU-18: Editar rol de proceso
    RolProcesoResponseDTO editar(
            Long rolProcesoId,
            RolProcesoRequestDTO request,
            Long empresaId,
            RolUsuario rol);

    // HU-19: Eliminar rol de proceso
    EliminacionRolProcesoResponseDTO eliminar(
            Long rolProcesoId,
            Long empresaId,
            RolUsuario rol);

    // HU-20: Consultar roles de proceso
    Page<RolProcesoListItemDTO> consultar(
            String nombre,
            Pageable pageable,
            Long empresaId);

    // Nombrar una lane de un proceso con un rol del catalogo de la empresa
    LaneDTO crearLaneConRol(
            Long procesoId,
            Long rolProcesoId,
            Long empresaId,
            RolUsuario rol);
}
