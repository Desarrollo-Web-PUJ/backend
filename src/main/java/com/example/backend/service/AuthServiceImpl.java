package com.example.backend.service;

import com.example.backend.dto.LoginRequestDTO;
import com.example.backend.dto.LoginResponseDTO;
import com.example.backend.entity.Usuario;
import com.example.backend.exception.CredencialesInvalidasException;
import com.example.backend.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {

        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public LoginResponseDTO iniciarSesion(LoginRequestDTO request) {

        Usuario usuario = usuarioRepository.findByCorreo(request.getCorreo())
                .orElseThrow(() ->
                        new CredencialesInvalidasException(
                                "Correo o contraseña incorrectos"
                        ));

        if (!usuario.getActivo()) {
            throw new CredencialesInvalidasException(
                    "Correo o contraseña incorrectos"
            );
        }

        if (!passwordEncoder.matches(
                request.getPassword(),
                usuario.getPassword())) {

            throw new CredencialesInvalidasException(
                    "Correo o contraseña incorrectos"
            );
        }

        return new LoginResponseDTO(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getRol(),
                usuario.getEmpresa().getId()
        );
    }
}