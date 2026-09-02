package com.example.backend.controller;

import com.example.backend.dto.LoginRequestDTO;
import com.example.backend.dto.LoginResponseDTO;
import com.example.backend.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> iniciarSesion(
            @Valid @RequestBody LoginRequestDTO request,
            HttpSession session) {

        LoginResponseDTO response = authService.iniciarSesion(request);

        session.setAttribute("usuarioId", response.getUsuarioId());
        session.setAttribute("empresaId", response.getEmpresaId());
        session.setAttribute("rol", response.getRol());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> cerrarSesion(HttpSession session) {

        session.invalidate();

        return ResponseEntity.ok("Sesión cerrada correctamente");
    }
}
