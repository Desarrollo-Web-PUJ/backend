package com.example.backend.service;

import com.example.backend.dto.UsuarioRegistroRequestDTO;
import com.example.backend.dto.UsuarioResponseDTO;
import com.example.backend.entity.Empresa;
import com.example.backend.entity.Usuario;
import com.example.backend.exception.RecursoDuplicadoException;
import com.example.backend.exception.RecursoNoEncontradoException;
import com.example.backend.repository.EmpresaRepository;
import com.example.backend.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository,
                               EmpresaRepository empresaRepository,
                               PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.empresaRepository = empresaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public UsuarioResponseDTO registrarUsuario(Long empresaId, UsuarioRegistroRequestDTO request) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe una empresa con el id " + empresaId));

        if (usuarioRepository.existsByCorreo(request.getCorreo())) {
            throw new RecursoDuplicadoException("Ya existe un usuario registrado con ese correo");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setCorreo(request.getCorreo());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setRol(request.getRol());
        usuario.setActivo(true);
        usuario.setEmpresa(empresa);
        usuario = usuarioRepository.save(usuario);

        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getRol(),
                usuario.getActivo(),
                empresa.getId()
        );
    }
}
