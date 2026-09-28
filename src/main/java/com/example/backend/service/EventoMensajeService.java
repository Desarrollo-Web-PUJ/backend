package com.example.backend.service;

import com.example.backend.dto.EliminacionEventoMensajeResponseDTO;
import com.example.backend.dto.EventoMensajeRequestDTO;
import com.example.backend.dto.EventoMensajeResponseDTO;
import com.example.backend.entity.RolUsuario;

import java.util.List;

public interface EventoMensajeService {

    List<EventoMensajeResponseDTO> listarPorProceso(Long procesoId, Long empresaId);

    EventoMensajeResponseDTO crearEvento(Long procesoId, EventoMensajeRequestDTO request,
                                          Long empresaId, RolUsuario rol);

    EventoMensajeResponseDTO editarEvento(Long procesoId, Long eventoId,
                                           EventoMensajeRequestDTO request,
                                           Long empresaId, RolUsuario rol);

    EliminacionEventoMensajeResponseDTO eliminarEvento(Long procesoId, Long eventoId,
                                                        Long empresaId, RolUsuario rol);
}