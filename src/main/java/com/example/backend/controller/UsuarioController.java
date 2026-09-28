package com.example.backend.controller;

import com.example.backend.dto.UsuarioActualizarRolRequestDTO;
import com.example.backend.dto.UsuarioRegistroRequestDTO;
import com.example.backend.dto.UsuarioResponseDTO;
import com.example.backend.entity.RolUsuario;
import com.example.backend.exception.PermisoDenegadoException;
import com.example.backend.exception.SesionNoAutenticadaException;
import com.example.backend.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/empresas/{empresaId}/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // HU-02: Registro de un nuevo usuario/colaborador dentro de una empresa ya existente
    @PostMapping("/registro")
    public ResponseEntity<UsuarioResponseDTO> registrarUsuario(
            @PathVariable Long empresaId,
            @Valid @RequestBody UsuarioRegistroRequestDTO request,
            HttpSession session) {

        validarEmpresaSesion(empresaId, session);
        RolUsuario rolSesion = obtenerRol(session);

        UsuarioResponseDTO response = usuarioService.registrarUsuario(empresaId, rolSesion, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> listarUsuarios(
            @PathVariable Long empresaId,
            HttpSession session) {

        validarEmpresaSesion(empresaId, session);
        validarAdministrador(obtenerRol(session));

        return ResponseEntity.ok(usuarioService.listarUsuarios(empresaId));
    }

    @PutMapping("/{usuarioId}/rol")
    public ResponseEntity<UsuarioResponseDTO> cambiarRol(
            @PathVariable Long empresaId,
            @PathVariable Long usuarioId,
            @Valid @RequestBody UsuarioActualizarRolRequestDTO request,
            HttpSession session) {

        validarEmpresaSesion(empresaId, session);
        RolUsuario rolSesion = obtenerRol(session);

        return ResponseEntity.ok(usuarioService.cambiarRol(empresaId, usuarioId, rolSesion, request));
    }

    @PatchMapping("/{usuarioId}/desactivar")
    public ResponseEntity<UsuarioResponseDTO> desactivarUsuario(
            @PathVariable Long empresaId,
            @PathVariable Long usuarioId,
            HttpSession session) {

        validarEmpresaSesion(empresaId, session);
        RolUsuario rolSesion = obtenerRol(session);

        return ResponseEntity.ok(usuarioService.desactivarUsuario(empresaId, usuarioId, rolSesion));
    }

    private void validarEmpresaSesion(Long empresaId, HttpSession session) {
        Long empresaSesionId = (Long) session.getAttribute("empresaId");

        if (empresaSesionId == null) {
            throw new SesionNoAutenticadaException("No hay una empresa asociada a la sesión");
        }

        if (!empresaSesionId.equals(empresaId)) {
            throw new PermisoDenegadoException("No puede gestionar usuarios de otra empresa");
        }
    }

    private RolUsuario obtenerRol(HttpSession session) {
        RolUsuario rol = (RolUsuario) session.getAttribute("rol");

        if (rol == null) {
            throw new SesionNoAutenticadaException("No hay un rol asociado a la sesión");
        }

        return rol;
    }

    private void validarAdministrador(RolUsuario rol) {
        if (rol != RolUsuario.ADMINISTRADOR) {
            throw new PermisoDenegadoException("Solo un administrador puede gestionar usuarios");
        }
    }
}
