package com.example.backend.service;

import com.example.backend.dto.LoginRequestDTO;
import com.example.backend.dto.LoginResponseDTO;

public interface AuthService {

    LoginResponseDTO iniciarSesion(LoginRequestDTO request);
}
