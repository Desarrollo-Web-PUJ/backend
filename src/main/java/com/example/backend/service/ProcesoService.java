package com.example.backend.service;

import com.example.backend.dto.*;
import com.example.backend.entity.EstadoProceso;
import com.example.backend.entity.RolUsuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProcesoService {

    ProcesoResponseDTO crearProceso(Long empresaId, Long usuarioId, ProcesoCrearRequestDTO request);

    ProcesoResponseDTO editarProceso(Long procesoId, Long empresaId, Long usuarioId,
                                      RolUsuario rol, ProcesoEditarRequestDTO request);

    void eliminarProceso(Long procesoId, Long empresaId, Long usuarioId, RolUsuario rol);

    Page<ProcesoListItemDTO> listarProcesos(Long empresaId, String nombre, EstadoProceso estado,
                                             String categoria, boolean incluirInactivos, Pageable pageable);

    ProcesoDetalleDTO obtenerDetalle(Long procesoId, Long empresaId);

    List<HistorialProcesoDTO> obtenerHistorial(Long procesoId, Long empresaId);
}
