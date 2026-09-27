package com.example.backend.controller;

import com.example.backend.dto.ArcoRequestDTO;
import com.example.backend.dto.ArcoResponseDTO;
import com.example.backend.dto.EliminacionArcoResponseDTO;
import com.example.backend.entity.RolUsuario;
import com.example.backend.exception.AccesoDenegadoException;
import com.example.backend.exception.OperacionInvalidaException;
import com.example.backend.service.ArcoService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/procesos/{procesoId}/arcos")
public class ArcoController {

    private final ArcoService arcoService;

    public ArcoController(ArcoService arcoService) {
        this.arcoService = arcoService;
    }

    // HU-11: Crear arco
    @PostMapping
    public ResponseEntity<ArcoResponseDTO> crearArco(
            @PathVariable Long procesoId,
            @Valid @RequestBody ArcoRequestDTO request,
            HttpSession session) {

        Long empresaId = obtenerEmpresaId(session);
        RolUsuario rol = obtenerRol(session);

        ArcoResponseDTO response = arcoService.crearArco(
                procesoId,
                request,
                empresaId,
                rol
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // HU-12: Editar arco
    @PutMapping("/{arcoId}")
    public ResponseEntity<ArcoResponseDTO> editarArco(
            @PathVariable Long procesoId,
            @PathVariable Long arcoId,
            @Valid @RequestBody ArcoRequestDTO request,
            HttpSession session) {

        Long empresaId = obtenerEmpresaId(session);
        RolUsuario rol = obtenerRol(session);

        ArcoResponseDTO response = arcoService.editarArco(
                procesoId,
                arcoId,
                request,
                empresaId,
                rol
        );

        return ResponseEntity.ok(response);
    }

    // HU-13: Eliminar arco
    @DeleteMapping("/{arcoId}")
    public ResponseEntity<EliminacionArcoResponseDTO> eliminarArco(
            @PathVariable Long procesoId,
            @PathVariable Long arcoId,
            @RequestParam(defaultValue = "false") boolean confirmar,
            HttpSession session) {

        if (!confirmar) {
            throw new OperacionInvalidaException(
                    "Debe confirmar la eliminación del arco"
            );
        }

        Long empresaId = obtenerEmpresaId(session);
        RolUsuario rol = obtenerRol(session);

        EliminacionArcoResponseDTO response = arcoService.eliminarArco(
                procesoId,
                arcoId,
                empresaId,
                rol
        );

        return ResponseEntity.ok(response);
    }

    private Long obtenerEmpresaId(HttpSession session) {

        Long empresaId = (Long) session.getAttribute("empresaId");

        if (empresaId == null) {
            throw new AccesoDenegadoException(
                    "Debe iniciar sesión para realizar esta operación"
            );
        }

        return empresaId;
    }

    private RolUsuario obtenerRol(HttpSession session) {

        RolUsuario rol = (RolUsuario) session.getAttribute("rol");

        if (rol == null) {
            throw new AccesoDenegadoException(
                    "Debe iniciar sesión para realizar esta operación"
            );
        }

        return rol;
    }
}