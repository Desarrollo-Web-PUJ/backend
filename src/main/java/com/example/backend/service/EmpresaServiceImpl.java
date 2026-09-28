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
        String nombre = normalizar(request.getNombre());
        String nit = normalizar(request.getNit());
        String correoContacto = normalizar(request.getCorreoContacto());
        String adminNombre = normalizar(request.getAdminNombre());
        String adminCorreo = normalizar(request.getAdminCorreo());

        if (empresaRepository.existsByNitIgnoreCase(nit)) {
            throw new RecursoDuplicadoException("Ya existe una empresa registrada con ese NIT");
        }
        if (usuarioRepository.existsByCorreoIgnoreCase(adminCorreo)) {
            throw new RecursoDuplicadoException("Ya existe un usuario registrado con ese correo");
        }

        Empresa empresa = new Empresa();
        empresa.setNombre(nombre);
        empresa.setNit(nit);
        empresa.setCorreoContacto(correoContacto);
        empresa = empresaRepository.save(empresa);

        Usuario admin = new Usuario();
        admin.setNombre(adminNombre);
        admin.setCorreo(adminCorreo);
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

    private String normalizar(String valor) {
        return valor == null ? null : valor.trim();
    }
}
