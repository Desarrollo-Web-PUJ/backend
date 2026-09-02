package com.example.backend.service;

import com.example.backend.dto.EmpresaRegistroRequestDTO;
import com.example.backend.dto.EmpresaResponseDTO;
import com.example.backend.entity.Empresa;
import com.example.backend.entity.RolUsuario;
import com.example.backend.entity.Usuario;
import com.example.backend.exception.RecursoDuplicadoException;
import com.example.backend.repository.EmpresaRepository;
import com.example.backend.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmpresaServiceImpl implements EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public EmpresaServiceImpl(EmpresaRepository empresaRepository,
                               UsuarioRepository usuarioRepository,
                               PasswordEncoder passwordEncoder) {
        this.empresaRepository = empresaRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public EmpresaResponseDTO registrarEmpresa(EmpresaRegistroRequestDTO request) {
        if (empresaRepository.existsByNit(request.getNit())) {
            throw new RecursoDuplicadoException("Ya existe una empresa registrada con ese NIT");
        }
        if (usuarioRepository.existsByCorreo(request.getAdminCorreo())) {
            throw new RecursoDuplicadoException("Ya existe un usuario registrado con ese correo");
        }

        Empresa empresa = new Empresa();
        empresa.setNombre(request.getNombre());
        empresa.setNit(request.getNit());
        empresa.setCorreoContacto(request.getCorreoContacto());
        empresa = empresaRepository.save(empresa);

        Usuario admin = new Usuario();
        admin.setNombre(request.getAdminNombre());
        admin.setCorreo(request.getAdminCorreo());
        admin.setPassword(passwordEncoder.encode(request.getAdminPassword()));
        admin.setRol(RolUsuario.ADMINISTRADOR);
        admin.setActivo(true);
        admin.setEmpresa(empresa);
        admin = usuarioRepository.save(admin);

        return new EmpresaResponseDTO(
                empresa.getId(),
                empresa.getNombre(),
                empresa.getNit(),
                empresa.getCorreoContacto(),
                admin.getId()
        );
    }
}
