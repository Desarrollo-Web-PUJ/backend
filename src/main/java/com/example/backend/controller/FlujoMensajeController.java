package com.example.backend.controller;

import com.example.backend.dto.EliminacionFlujoMensajeResponseDTO;
import com.example.backend.dto.FlujoMensajeRequestDTO;
import com.example.backend.dto.FlujoMensajeResponseDTO;
import com.example.backend.entity.RolUsuario;
import com.example.backend.exception.OperacionInvalidaException;
import com.example.backend.exception.SesionNoAutenticadaException;
import com.example.backend.service.FlujoMensajeService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/procesos/{procesoId}/flujos-mensaje")
public class FlujoMensajeController {

    private final FlujoMensajeService flujoService;

    public FlujoMensajeController(FlujoMensajeService flujoService) {
        this.flujoService = flujoService;
    }

    // HU-25: listar flujos de mensaje
    @GetMapping
    public ResponseEntity<List<FlujoMensajeResponseDTO>> listar(
            @PathVariable Long procesoId,
            HttpSession session) {
        Long empresaId = obtenerEmpresaId(session);
        return ResponseEntity.ok(flujoService.listarPorProceso(procesoId, empresaId));
    }

    // HU-25 / HU-26: crear flujo de mensaje
    @PostMapping
    public ResponseEntity<FlujoMensajeResponseDTO> crear(
            @PathVariable Long procesoId,
            @Valid @RequestBody FlujoMensajeRequestDTO request,
            HttpSession session) {
        Long empresaId = obtenerEmpresaId(session);
        RolUsuario rol = obtenerRol(session);
        FlujoMensajeResponseDTO dto = flujoService.crearFlujo(procesoId, request, empresaId, rol);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PutMapping("/{flujoId}")
    public ResponseEntity<FlujoMensajeResponseDTO> editar(
            @PathVariable Long procesoId,
            @PathVariable Long flujoId,
            @Valid @RequestBody FlujoMensajeRequestDTO request,
            HttpSession session) {
        Long empresaId = obtenerEmpresaId(session);
        RolUsuario rol = obtenerRol(session);
        return ResponseEntity.ok(flujoService.editarFlujo(procesoId, flujoId, request, empresaId, rol));
    }

    @DeleteMapping("/{flujoId}")
    public ResponseEntity<EliminacionFlujoMensajeResponseDTO> eliminar(
            @PathVariable Long procesoId,
            @PathVariable Long flujoId,
            @RequestParam(defaultValue = "false") boolean confirmar,
            HttpSession session) {
        if (!confirmar) {
            throw new OperacionInvalidaException("Debe confirmar la eliminación del flujo de mensaje");
        }
        Long empresaId = obtenerEmpresaId(session);
        RolUsuario rol = obtenerRol(session);
        return ResponseEntity.ok(flujoService.eliminarFlujo(procesoId, flujoId, empresaId, rol));
    }

    private Long obtenerEmpresaId(HttpSession session) {
        Long empresaId = (Long) session.getAttribute("empresaId");
        if (empresaId == null) throw new SesionNoAutenticadaException("No hay empresa en la sesión");
        return empresaId;
    }

    private RolUsuario obtenerRol(HttpSession session) {
        RolUsuario rol = (RolUsuario) session.getAttribute("rol");
        if (rol == null) throw new SesionNoAutenticadaException("No hay rol en la sesión");
        return rol;
    }
}
