package com.example.backend.service;

import com.example.backend.dto.EdicionGatewayResponseDTO;
import com.example.backend.dto.EliminacionGatewayResponseDTO;
import com.example.backend.dto.GatewayRequestDTO;
import com.example.backend.dto.GatewayResponseDTO;
import com.example.backend.entity.RolUsuario;

import java.util.List;

public interface GatewayService {

    GatewayResponseDTO crearGateway(
            Long procesoId,
            GatewayRequestDTO request,
            Long empresaId,
            RolUsuario rol
    );

    List<GatewayResponseDTO> listarPorProceso(
            Long procesoId,
            Long empresaId
    );

    EdicionGatewayResponseDTO editarGateway(
            Long procesoId,
            Long gatewayId,
            GatewayRequestDTO request,
            Long empresaId,
            RolUsuario rol
    );

    EliminacionGatewayResponseDTO eliminarGateway(
            Long procesoId,
            Long gatewayId,
            Long empresaId,
            RolUsuario rol,
            boolean confirmar
    );
}
