package com.example.backend.service;

import com.example.backend.dto.UsuarioActualizarRolRequestDTO;
import com.example.backend.dto.UsuarioRegistroRequestDTO;
import com.example.backend.dto.UsuarioResponseDTO;
import com.example.backend.entity.Empresa;
import com.example.backend.entity.RolUsuario;
import com.example.backend.entity.Usuario;
import com.example.backend.exception.PermisoDenegadoException;
import com.example.backend.exception.RecursoDuplicadoException;
import com.example.backend.exception.RecursoNoEncontradoException;
import com.example.backend.repository.EmpresaRepository;
import com.example.backend.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    public UsuarioResponseDTO registrarUsuario(Long empresaId, RolUsuario rolSesion, UsuarioRegistroRequestDTO request) {
        validarAdministrador(rolSesion);

        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe una empresa con el id " + empresaId));

        String correo = normalizarCorreo(request.getCorreo());

        if (usuarioRepository.existsByCorreoIgnoreCase(correo)) {
            throw new RecursoDuplicadoException("Ya existe un usuario registrado con ese correo");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre().trim());
        usuario.setCorreo(correo);
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setRol(request.getRol());
        usuario.setActivo(true);
        usuario.setEmpresa(empresa);
        usuario = usuarioRepository.save(usuario);

        return toDTO(usuario);
    }

    @Override
    public List<UsuarioResponseDTO> listarUsuarios(Long empresaId) {
        return usuarioRepository.findByEmpresaIdOrderByNombreAsc(empresaId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public UsuarioResponseDTO cambiarRol(Long empresaId, Long usuarioId, RolUsuario rolSesion,
                                         UsuarioActualizarRolRequestDTO request) {
        validarAdministrador(rolSesion);

        Usuario usuario = usuarioRepository.findByIdAndEmpresaId(usuarioId, empresaId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El usuario no existe en la empresa"));

        usuario.setRol(request.getRol());

        return toDTO(usuarioRepository.save(usuario));
    }

    @Override
    @Transactional
    public UsuarioResponseDTO desactivarUsuario(Long empresaId, Long usuarioId, RolUsuario rolSesion) {
        validarAdministrador(rolSesion);

        Usuario usuario = usuarioRepository.findByIdAndEmpresaId(usuarioId, empresaId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El usuario no existe en la empresa"));

        usuario.setActivo(false);

        return toDTO(usuarioRepository.save(usuario));
    }

    private void validarAdministrador(RolUsuario rolSesion) {
        if (rolSesion != RolUsuario.ADMINISTRADOR) {
            throw new PermisoDenegadoException("Solo un administrador puede gestionar usuarios");
        }
    }

    private String normalizarCorreo(String correo) {
        return correo.trim().toLowerCase();
    }

    private UsuarioResponseDTO toDTO(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getRol(),
                usuario.getActivo(),
                usuario.getEmpresa().getId()
        );
    }
}
