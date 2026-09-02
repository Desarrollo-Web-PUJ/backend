package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EmpresaResponseDTO {
    private Long id;
    private String nombre;
    private String nit;
    private String correoContacto;
    private Long adminUsuarioId;
}
