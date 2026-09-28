package com.example.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.EliminacionPoolResponseDTO;
import com.example.backend.dto.PoolRequestDTO;
import com.example.backend.dto.PoolResponseDTO;
import com.example.backend.dto.RolProcesoResponseDTO;
import com.example.backend.entity.RolUsuario;
import com.example.backend.exception.SesionNoAutenticadaException;
import com.example.backend.service.PoolService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/procesos/{procesoId}/pools")
public class PoolController {

    private final PoolService poolService;

    public PoolController(PoolService poolService) {
        this.poolService = poolService;
    }

    // HU-21: Listar pools del proceso
    @GetMapping
    public ResponseEntity<List<PoolResponseDTO>> listar(
            @PathVariable Long procesoId,
            HttpSession session) {

        Long empresaId = obtenerEmpresaId(session);
        return ResponseEntity.ok(poolService.listarPorProceso(procesoId, empresaId));
    }

    // HU-24: Roles de proceso activos que pueden asignarse a lanes del pool
    @GetMapping("/{poolId}/roles-disponibles")
    public ResponseEntity<List<RolProcesoResponseDTO>> rolesDisponibles(
            @PathVariable Long procesoId,
            @PathVariable Long poolId,
            HttpSession session) {

        Long empresaId = obtenerEmpresaId(session);
        return ResponseEntity.ok(poolService.listarRolesDisponibles(procesoId, poolId, empresaId));
    }

    // HU-21: Crear pool
    @PostMapping
    public ResponseEntity<PoolResponseDTO> crear(
            @PathVariable Long procesoId,
            @Valid @RequestBody PoolRequestDTO request,
            HttpSession session) {

        Long empresaId = obtenerEmpresaId(session);
        RolUsuario rol = obtenerRol(session);

        PoolResponseDTO response = poolService.crearPool(procesoId, request, empresaId, rol);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // HU-21: Editar pool
    @PutMapping("/{poolId}")
    public ResponseEntity<PoolResponseDTO> editar(
            @PathVariable Long procesoId,
            @PathVariable Long poolId,
            @Valid @RequestBody PoolRequestDTO request,
            HttpSession session) {

        Long empresaId = obtenerEmpresaId(session);
        RolUsuario rol = obtenerRol(session);

        return ResponseEntity.ok(poolService.editarPool(procesoId, poolId, request, empresaId, rol));
    }

    // HU-21: Eliminar pool
    @DeleteMapping("/{poolId}")
    public ResponseEntity<EliminacionPoolResponseDTO> eliminar(
            @PathVariable Long procesoId,
            @PathVariable Long poolId,
            HttpSession session) {

        Long empresaId = obtenerEmpresaId(session);
        RolUsuario rol = obtenerRol(session);

        return ResponseEntity.ok(poolService.eliminarPool(procesoId, poolId, empresaId, rol));
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
