package com.example.backend.dto;

import com.example.backend.entity.RolUsuario;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponseDTO {

    private Long usuarioId;
    private String nombre;
    private String correo;
    private RolUsuario rol;
    private Long empresaId;

    public LoginResponseDTO(
            Long usuarioId,
            String nombre,
            String correo,
            RolUsuario rol,
            Long empresaId) {

        this.usuarioId = usuarioId;
        this.nombre = nombre;
        this.correo = correo;
        this.rol = rol;
        this.empresaId = empresaId;
    }
}