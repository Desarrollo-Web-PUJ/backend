package com.example.backend.controller;

import com.example.backend.dto.EmpresaRegistroRequestDTO;
import com.example.backend.dto.EmpresaResponseDTO;
import com.example.backend.service.EmpresaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/empresas")
public class EmpresaController {

    private final EmpresaService empresaService;

    public EmpresaController(EmpresaService empresaService) {
        this.empresaService = empresaService;
    }

    // HU-01: Registro de empresa (crea la empresa junto con su usuario administrador inicial)
    @PostMapping("/registro")
    public ResponseEntity<EmpresaResponseDTO> registrarEmpresa(
            @Valid @RequestBody EmpresaRegistroRequestDTO request) {
        EmpresaResponseDTO response = empresaService.registrarEmpresa(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
