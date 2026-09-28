package com.example.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.CompartirProcesoRequestDTO;
import com.example.backend.dto.ProcesoCompartidoResponseDTO;
import com.example.backend.entity.RolUsuario;
import com.example.backend.exception.SesionNoAutenticadaException;
import com.example.backend.service.ProcesoCompartidoService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@RestController
public class ProcesoCompartidoController {

    private final ProcesoCompartidoService procesoCompartidoService;

    public ProcesoCompartidoController(ProcesoCompartidoService procesoCompartidoService) {
        this.procesoCompartidoService = procesoCompartidoService;
    }

    // HU-23: Ver con quién está compartido un proceso propio
    @GetMapping("/api/procesos/{procesoId}/comparticiones")
    public ResponseEntity<List<ProcesoCompartidoResponseDTO>> listar(
            @PathVariable Long procesoId,
            HttpSession session) {

        Long empresaId = obtenerEmpresaId(session);
        return ResponseEntity.ok(procesoCompartidoService.listarComparticiones(procesoId, empresaId));
    }

    // HU-23: Compartir el proceso con otra empresa (solo administrador)
    @PostMapping("/api/procesos/{procesoId}/comparticiones")
    public ResponseEntity<ProcesoCompartidoResponseDTO> compartir(
            @PathVariable Long procesoId,
            @Valid @RequestBody CompartirProcesoRequestDTO request,
            HttpSession session) {

        Long empresaId = obtenerEmpresaId(session);
        RolUsuario rol = obtenerRol(session);

        ProcesoCompartidoResponseDTO response =
                procesoCompartidoService.compartir(procesoId, request, empresaId, rol);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // HU-23: Dejar de compartir
    @DeleteMapping("/api/procesos/{procesoId}/comparticiones/{empresaDestinoId}")
    public ResponseEntity<Void> dejarDeCompartir(
            @PathVariable Long procesoId,
            @PathVariable Long empresaDestinoId,
            HttpSession session) {

        Long empresaId = obtenerEmpresaId(session);
        RolUsuario rol = obtenerRol(session);

        procesoCompartidoService.dejarDeCompartir(procesoId, empresaDestinoId, empresaId, rol);
        return ResponseEntity.noContent().build();
    }

    // HU-23: Procesos de otras empresas compartidos con la mía (solo lectura)
    @GetMapping("/api/procesos/compartidos-conmigo")
    public ResponseEntity<List<ProcesoCompartidoResponseDTO>> compartidosConmigo(HttpSession session) {
        Long empresaId = obtenerEmpresaId(session);
        return ResponseEntity.ok(procesoCompartidoService.listarCompartidosConmigo(empresaId));
    }

    private Long obtenerEmpresaId(HttpSession session) {
        Long empresaId = (Long) session.getAttribute("empresaId");
        if (empresaId == null) {
            throw new SesionNoAutenticadaException("No hay una empresa asociada a la sesión");
        }
        return empresaId;
    }

    private RolUsuario obtenerRol(HttpSession session) {
        RolUsuario rol = (RolUsuario) session.getAttribute("rol");
        if (rol == null) {
            throw new SesionNoAutenticadaException("No hay un rol asociado a la sesión");
        }
        return rol;
    }
}