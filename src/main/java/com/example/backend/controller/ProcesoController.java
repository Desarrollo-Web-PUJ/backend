package com.example.backend.controller;

import com.example.backend.dto.HistorialProcesoDTO;
import com.example.backend.dto.ProcesoCrearRequestDTO;
import com.example.backend.dto.ProcesoDetalleDTO;
import com.example.backend.dto.ProcesoEditarRequestDTO;
import com.example.backend.dto.ProcesoListItemDTO;
import com.example.backend.dto.ProcesoResponseDTO;
import com.example.backend.entity.EstadoProceso;
import com.example.backend.entity.RolUsuario;
import com.example.backend.service.ProcesoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api/procesos")
public class ProcesoController {

    private final ProcesoService procesoService;

    public ProcesoController(ProcesoService procesoService) {
        this.procesoService = procesoService;
    }

    private Long obtenerUsuarioId(HttpSession session) {
        return (Long) session.getAttribute("usuarioId");
    }

    private Long obtenerEmpresaId(HttpSession session) {
        return (Long) session.getAttribute("empresaId");
    }

    private RolUsuario obtenerRol(HttpSession session) {
        return (RolUsuario) session.getAttribute("rol");
    }

    // HU-07: Consultar procesos
    @GetMapping
    @ResponseBody
    public ResponseEntity<Page<ProcesoListItemDTO>> listar(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) EstadoProceso estado,
            @RequestParam(required = false) String categoria,
            @RequestParam(defaultValue = "false") boolean incluirInactivos,
            @PageableDefault(size = 10) Pageable pageable,
            HttpSession session) {

        Long empresaId = obtenerEmpresaId(session);

        if (empresaId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Page<ProcesoListItemDTO> procesos = procesoService.listarProcesos(
                empresaId,
                nombre,
                estado,
                categoria,
                incluirInactivos,
                pageable
        );

        return ResponseEntity.ok(procesos);
    }

    // HU-04: Crear proceso
    @PostMapping
    @ResponseBody
    public ResponseEntity<ProcesoResponseDTO> crear(
            @RequestBody ProcesoCrearRequestDTO request,
            HttpSession session) {

        Long empresaId = obtenerEmpresaId(session);
        Long usuarioId = obtenerUsuarioId(session);
        RolUsuario rol = obtenerRol(session);

        if (empresaId == null || usuarioId == null || rol == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ProcesoResponseDTO proceso = procesoService.crearProceso(
                empresaId,
                usuarioId,
                rol,
                request
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(proceso);
    }

    // HU-07: Consultar detalle de un proceso
    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<ProcesoDetalleDTO> detalle(
            @PathVariable Long id,
            HttpSession session) {

        Long empresaId = obtenerEmpresaId(session);

        if (empresaId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ProcesoDetalleDTO proceso = procesoService.obtenerDetalle(
                id,
                empresaId
        );

        return ResponseEntity.ok(proceso);
    }

    // HU-05: Editar proceso
    @PutMapping("/{id}")
    @ResponseBody
    public ResponseEntity<ProcesoResponseDTO> editar(
            @PathVariable Long id,
            @RequestBody ProcesoEditarRequestDTO request,
            HttpSession session) {

        Long empresaId = obtenerEmpresaId(session);
        Long usuarioId = obtenerUsuarioId(session);
        RolUsuario rol = obtenerRol(session);

        if (empresaId == null || usuarioId == null || rol == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ProcesoResponseDTO proceso = procesoService.editarProceso(
                id,
                empresaId,
                usuarioId,
                rol,
                request
        );

        return ResponseEntity.ok(proceso);
    }

    // HU-06: Eliminar proceso
    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id,
            HttpSession session) {

        Long empresaId = obtenerEmpresaId(session);
        Long usuarioId = obtenerUsuarioId(session);
        RolUsuario rol = obtenerRol(session);

        if (empresaId == null || usuarioId == null || rol == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        procesoService.eliminarProceso(
                id,
                empresaId,
                usuarioId,
                rol
        );

        return ResponseEntity.noContent().build();
    }

    // Consultar historial de un proceso
    @GetMapping("/{id}/historial")
    @ResponseBody
    public ResponseEntity<List<HistorialProcesoDTO>> historial(
            @PathVariable Long id,
            HttpSession session) {

        Long empresaId = obtenerEmpresaId(session);

        if (empresaId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<HistorialProcesoDTO> historial =
                procesoService.obtenerHistorial(id, empresaId);

        return ResponseEntity.ok(historial);
    }
}
