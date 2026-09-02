package com.example.backend.dto;

import com.example.backend.entity.RolUsuario;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UsuarioResponseDTO {
    private Long id;
    private String nombre;
    private String correo;
    private RolUsuario rol;
    private Boolean activo;
    private Long empresaId;
}
