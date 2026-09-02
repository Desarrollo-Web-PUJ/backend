package com.example.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmpresaRegistroRequestDTO {

    @NotBlank(message = "El nombre de la empresa es obligatorio")
    private String nombre;

    @NotBlank(message = "El NIT es obligatorio")
    private String nit;

    @NotBlank(message = "El correo de contacto es obligatorio")
    @Email(message = "El correo de contacto no tiene un formato valido")
    private String correoContacto;

    @NotBlank(message = "El nombre del administrador es obligatorio")
    private String adminNombre;

    @NotBlank(message = "El correo del administrador es obligatorio")
    @Email(message = "El correo del administrador no tiene un formato valido")
    private String adminCorreo;

    @NotBlank(message = "La contrasena del administrador es obligatoria")
    @Size(min = 8, message = "La contrasena debe tener al menos 8 caracteres")
    private String adminPassword;
}
