package com.example.backend.service;

import com.example.backend.dto.UsuarioRegistroRequestDTO;
import com.example.backend.dto.UsuarioResponseDTO;

public interface UsuarioService {
    UsuarioResponseDTO registrarUsuario(Long empresaId, UsuarioRegistroRequestDTO request);
}
