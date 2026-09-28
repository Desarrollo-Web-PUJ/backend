package com.example.backend.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.dto.CompartirProcesoRequestDTO;
import com.example.backend.dto.ProcesoCompartidoResponseDTO;
import com.example.backend.entity.Empresa;
import com.example.backend.entity.HistorialProceso;
import com.example.backend.entity.Proceso;
import com.example.backend.entity.ProcesoCompartido;
import com.example.backend.entity.RolUsuario;
import com.example.backend.exception.OperacionInvalidaException;
import com.example.backend.exception.PermisoDenegadoException;
import com.example.backend.exception.RecursoDuplicadoException;
import com.example.backend.exception.RecursoNoEncontradoException;
import com.example.backend.repository.EmpresaRepository;
import com.example.backend.repository.HistorialProcesoRepository;
import com.example.backend.repository.ProcesoCompartidoRepository;
import com.example.backend.repository.ProcesoRepository;

@Service
public class ProcesoCompartidoServiceImpl implements ProcesoCompartidoService {

    private final ProcesoCompartidoRepository procesoCompartidoRepository;
    private final ProcesoRepository procesoRepository;
    private final EmpresaRepository empresaRepository;
    private final HistorialProcesoRepository historialProcesoRepository;

    public ProcesoCompartidoServiceImpl(
            ProcesoCompartidoRepository procesoCompartidoRepository,
            ProcesoRepository procesoRepository,
            EmpresaRepository empresaRepository,
            HistorialProcesoRepository historialProcesoRepository) {
        this.procesoCompartidoRepository = procesoCompartidoRepository;
        this.procesoRepository = procesoRepository;
        this.empresaRepository = empresaRepository;
        this.historialProcesoRepository = historialProcesoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProcesoCompartidoResponseDTO> listarComparticiones(Long procesoId, Long empresaId) {
        Proceso proceso = obtenerProcesoPropio(procesoId, empresaId);
        return procesoCompartidoRepository.findByProcesoIdAndActivoTrue(proceso.getId()).stream()
                .map(this::convertirDTO)
                .toList();
    }

    @Override
    @Transactional
    public ProcesoCompartidoResponseDTO compartir(Long procesoId, CompartirProcesoRequestDTO request, Long empresaId, RolUsuario rol) {
        if (rol != RolUsuario.ADMINISTRADOR) {
            throw new PermisoDenegadoException("Solo un administrador puede compartir un proceso");
        }

        Proceso proceso = obtenerProcesoPropio(procesoId, empresaId);

        if (request == null || request.getEmpresaDestinoId() == null) {
            throw new OperacionInvalidaException("Debe indicar la empresa destino");
        }
        if (request.getEmpresaDestinoId().equals(empresaId)) {
            throw new OperacionInvalidaException("No se puede compartir un proceso con la misma empresa propietaria");
        }

        Empresa empresaDestino = empresaRepository.findById(request.getEmpresaDestinoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("La empresa destino no existe"));

        if (procesoCompartidoRepository.existsByProcesoIdAndEmpresaDestinoIdAndActivoTrue(
                proceso.getId(), empresaDestino.getId())) {
            throw new RecursoDuplicadoException("El proceso ya está compartido con esa empresa");
        }

        ProcesoCompartido compartido = new ProcesoCompartido();
        compartido.setProceso(proceso);
        compartido.setEmpresaDestino(empresaDestino);
        compartido.setFechaCompartido(LocalDateTime.now());
        compartido.setActivo(true);

        compartido = procesoCompartidoRepository.save(compartido);

        historialProcesoRepository.save(new HistorialProceso(
                proceso,
                "Se compartió el proceso (solo lectura) con la empresa \"" + empresaDestino.getNombre() + "\"",
                LocalDateTime.now()
        ));

        return convertirDTO(compartido);
    }

    @Override
    @Transactional
    public void dejarDeCompartir(Long procesoId, Long empresaDestinoId, Long empresaId, RolUsuario rol) {
        if (rol != RolUsuario.ADMINISTRADOR) {
            throw new PermisoDenegadoException("Solo un administrador puede modificar la compartición de un proceso");
        }

        Proceso proceso = obtenerProcesoPropio(procesoId, empresaId);

        ProcesoCompartido compartido = procesoCompartidoRepository
                .findByProcesoIdAndEmpresaDestinoIdAndActivoTrue(proceso.getId(), empresaDestinoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("El proceso no está compartido con esa empresa"));

        compartido.setActivo(false);
        procesoCompartidoRepository.save(compartido);

        historialProcesoRepository.save(new HistorialProceso(
                proceso,
                "Se dejó de compartir el proceso con la empresa #" + empresaDestinoId,
                LocalDateTime.now()
        ));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProcesoCompartidoResponseDTO> listarCompartidosConmigo(Long empresaId) {
        return procesoCompartidoRepository.findByEmpresaDestinoIdAndActivoTrue(empresaId).stream()
                .map(this::convertirDTO)
                .toList();
    }

    private Proceso obtenerProcesoPropio(Long procesoId, Long empresaId) {
        return procesoRepository.findByIdAndEmpresaId(procesoId, empresaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("El proceso no existe en la empresa indicada"));
    }

    private ProcesoCompartidoResponseDTO convertirDTO(ProcesoCompartido compartido) {
        ProcesoCompartidoResponseDTO dto = new ProcesoCompartidoResponseDTO();
        dto.setProcesoId(compartido.getProceso().getId());
        dto.setProcesoNombre(compartido.getProceso().getNombre());
        dto.setEmpresaDestinoId(compartido.getEmpresaDestino().getId());
        dto.setEmpresaDestinoNombre(compartido.getEmpresaDestino().getNombre());
        dto.setFechaCompartido(compartido.getFechaCompartido());
        return dto;
    }
}