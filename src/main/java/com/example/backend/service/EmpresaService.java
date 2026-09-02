package com.example.backend.service;

import com.example.backend.dto.EmpresaRegistroRequestDTO;
import com.example.backend.dto.EmpresaResponseDTO;

public interface EmpresaService {
    EmpresaResponseDTO registrarEmpresa(EmpresaRegistroRequestDTO request);
}
