package com.example.backend.service;

import com.example.backend.dto.EliminacionFlujoMensajeResponseDTO;
import com.example.backend.dto.FlujoMensajeRequestDTO;
import com.example.backend.dto.FlujoMensajeResponseDTO;
import com.example.backend.entity.RolUsuario;

import java.util.List;

public interface FlujoMensajeService {

    List<FlujoMensajeResponseDTO> listarPorProceso(Long procesoId, Long empresaId);

    FlujoMensajeResponseDTO crearFlujo(Long procesoId, FlujoMensajeRequestDTO request,
                                        Long empresaId, RolUsuario rol);

    EliminacionFlujoMensajeResponseDTO eliminarFlujo(Long procesoId, Long flujoId,
                                                      Long empresaId, RolUsuario rol);
}