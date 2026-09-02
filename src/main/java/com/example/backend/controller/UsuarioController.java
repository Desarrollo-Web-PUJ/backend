package com.example.backend.controller;

import com.example.backend.dto.UsuarioRegistroRequestDTO;
import com.example.backend.dto.UsuarioResponseDTO;
import com.example.backend.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
            @Valid @RequestBody UsuarioRegistroRequestDTO request) {
        UsuarioResponseDTO response = usuarioService.registrarUsuario(empresaId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
