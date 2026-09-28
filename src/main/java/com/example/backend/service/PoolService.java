package com.example.backend.service;

import java.util.List;

import com.example.backend.dto.EliminacionPoolResponseDTO;
import com.example.backend.dto.PoolRequestDTO;
import com.example.backend.dto.PoolResponseDTO;
import com.example.backend.entity.RolUsuario;

public interface PoolService {

    List<PoolResponseDTO> listarPorProceso(Long procesoId, Long empresaId);

    PoolResponseDTO crearPool(Long procesoId, PoolRequestDTO request, Long empresaId, RolUsuario rol);

    PoolResponseDTO editarPool(Long procesoId, Long poolId, PoolRequestDTO request, Long empresaId, RolUsuario rol);

    EliminacionPoolResponseDTO eliminarPool(Long procesoId, Long poolId, Long empresaId, RolUsuario rol);
}