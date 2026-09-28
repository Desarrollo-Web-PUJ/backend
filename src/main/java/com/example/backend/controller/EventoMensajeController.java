package com.example.backend.controller;

import com.example.backend.dto.EliminacionEventoMensajeResponseDTO;
import com.example.backend.dto.EventoMensajeRequestDTO;
import com.example.backend.dto.EventoMensajeResponseDTO;
import com.example.backend.entity.RolUsuario;
import com.example.backend.exception.OperacionInvalidaException;
import com.example.backend.exception.SesionNoAutenticadaException;
import com.example.backend.service.EventoMensajeService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/procesos/{procesoId}/eventos-mensaje")
public class EventoMensajeController {

    private final EventoMensajeService eventoService;

    public EventoMensajeController(EventoMensajeService eventoService) {
        this.eventoService = eventoService;
    }

    // HU-25, HU-26, HU-27: listar eventos de mensaje
    @GetMapping
    public ResponseEntity<List<EventoMensajeResponseDTO>> listar(
            @PathVariable Long procesoId,
            HttpSession session) {
        Long empresaId = obtenerEmpresaId(session);
        return ResponseEntity.ok(eventoService.listarPorProceso(procesoId, empresaId));
    }

    // HU-25, HU-26, HU-27: crear evento de mensaje
    @PostMapping
    public ResponseEntity<EventoMensajeResponseDTO> crear(
            @PathVariable Long procesoId,
            @Valid @RequestBody EventoMensajeRequestDTO request,
            HttpSession session) {
        Long empresaId = obtenerEmpresaId(session);
        RolUsuario rol = obtenerRol(session);
        EventoMensajeResponseDTO dto = eventoService.crearEvento(procesoId, request, empresaId, rol);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PutMapping("/{eventoId}")
    public ResponseEntity<EventoMensajeResponseDTO> editar(
            @PathVariable Long procesoId,
            @PathVariable Long eventoId,
            @Valid @RequestBody EventoMensajeRequestDTO request,
            HttpSession session) {
        Long empresaId = obtenerEmpresaId(session);
        RolUsuario rol = obtenerRol(session);
        return ResponseEntity.ok(eventoService.editarEvento(procesoId, eventoId, request, empresaId, rol));
    }

    @DeleteMapping("/{eventoId}")
    public ResponseEntity<EliminacionEventoMensajeResponseDTO> eliminar(
            @PathVariable Long procesoId,
            @PathVariable Long eventoId,
            @RequestParam(defaultValue = "false") boolean confirmar,
            HttpSession session) {
        if (!confirmar) {
            throw new OperacionInvalidaException("Debe confirmar la eliminación del evento");
        }
        Long empresaId = obtenerEmpresaId(session);
        RolUsuario rol = obtenerRol(session);
        return ResponseEntity.ok(eventoService.eliminarEvento(procesoId, eventoId, empresaId, rol));
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