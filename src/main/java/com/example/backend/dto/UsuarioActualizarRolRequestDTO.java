package com.example.backend.dto;

import com.example.backend.entity.RolUsuario;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioActualizarRolRequestDTO {

    @NotNull(message = "El rol es obligatorio")
    private RolUsuario rol;
}
