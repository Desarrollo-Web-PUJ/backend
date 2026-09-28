package com.example.backend.service;

import java.util.List;

import com.example.backend.dto.CompartirProcesoRequestDTO;
import com.example.backend.dto.ProcesoCompartidoResponseDTO;
import com.example.backend.entity.RolUsuario;

public interface ProcesoCompartidoService {

    List<ProcesoCompartidoResponseDTO> listarComparticiones(Long procesoId, Long empresaId);

    ProcesoCompartidoResponseDTO compartir(Long procesoId, CompartirProcesoRequestDTO request, Long empresaId, RolUsuario rol);

    void dejarDeCompartir(Long procesoId, Long empresaDestinoId, Long empresaId, RolUsuario rol);

    List<ProcesoCompartidoResponseDTO> listarCompartidosConmigo(Long empresaId);
}