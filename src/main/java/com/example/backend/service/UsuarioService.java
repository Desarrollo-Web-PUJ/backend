package com.example.backend.service;

import com.example.backend.dto.UsuarioActualizarRolRequestDTO;
import com.example.backend.dto.UsuarioRegistroRequestDTO;
import com.example.backend.dto.UsuarioResponseDTO;
import com.example.backend.entity.RolUsuario;

import java.util.List;

public interface UsuarioService {
    UsuarioResponseDTO registrarUsuario(Long empresaId, RolUsuario rolSesion, UsuarioRegistroRequestDTO request);

    List<UsuarioResponseDTO> listarUsuarios(Long empresaId);

    UsuarioResponseDTO cambiarRol(Long empresaId, Long usuarioId, RolUsuario rolSesion,
                                  UsuarioActualizarRolRequestDTO request);

    UsuarioResponseDTO desactivarUsuario(Long empresaId, Long usuarioId, RolUsuario rolSesion);
}
