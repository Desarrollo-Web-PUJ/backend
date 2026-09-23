package com.example.backend.service;

import com.example.backend.dto.*;
import com.example.backend.entity.*;
import com.example.backend.exception.PermisoDenegadoException;
import com.example.backend.repository.EmpresaRepository;
import com.example.backend.repository.HistorialProcesoRepository;
import com.example.backend.repository.ProcesoRepository;
import com.example.backend.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProcesoServiceImpl implements ProcesoService {

    private final ProcesoRepository procesoRepository;
    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;
    private final HistorialProcesoRepository historialProcesoRepository;
    private final LaneService laneService;
    private final ActividadService actividadService;

    public ProcesoServiceImpl(ProcesoRepository procesoRepository,
                               EmpresaRepository empresaRepository,
                               UsuarioRepository usuarioRepository,
                               HistorialProcesoRepository historialProcesoRepository,
                               LaneService laneService,
                               ActividadService actividadService) {
        this.procesoRepository = procesoRepository;
        this.empresaRepository = empresaRepository;
        this.usuarioRepository = usuarioRepository;
        this.historialProcesoRepository = historialProcesoRepository;
        this.laneService = laneService;
        this.actividadService = actividadService;
    }

    @Override
    @Transactional
    public ProcesoResponseDTO crearProceso(Long empresaId, Long usuarioId, ProcesoCrearRequestDTO request) {
        if (request.getNombre() == null || request.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del proceso es obligatorio");
        }
        if (request.getCategoria() == null || request.getCategoria().isBlank()) {
            throw new IllegalArgumentException("La categoria es obligatoria");
        }
        if (procesoRepository.existsByNombreAndEmpresaId(request.getNombre(), empresaId)) {
            throw new IllegalArgumentException("Ya existe un proceso con ese nombre en la empresa");
        }

        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException("La empresa no existe"));
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("El usuario no existe"));

        Proceso proceso = new Proceso();
        proceso.setNombre(request.getNombre());
        proceso.setDescripcion(request.getDescripcion());
        proceso.setCategoria(request.getCategoria());
        proceso.setEstado(EstadoProceso.BORRADOR);
        proceso.setActivo(true);
        proceso.setEmpresa(empresa);
        proceso = procesoRepository.save(proceso);

        registrarHistorial(proceso, "Se creo el proceso '" + proceso.getNombre() + "' (usuario: " + usuario.getNombre() + ")");

        return toResponseDTO(proceso);
    }

    @Override
    @Transactional
    public ProcesoResponseDTO editarProceso(Long procesoId, Long empresaId, Long usuarioId,
                                             RolUsuario rol, ProcesoEditarRequestDTO request) {
        if (rol != RolUsuario.ADMINISTRADOR && rol != RolUsuario.EDITOR) {
            throw new PermisoDenegadoException("No tienes permisos para editar procesos");
        }

        Proceso proceso = procesoRepository.findByIdAndEmpresaId(procesoId, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("El proceso no existe"));
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("El usuario no existe"));

        if (request.getNombre() == null || request.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del proceso es obligatorio");
        }
        if (request.getCategoria() == null || request.getCategoria().isBlank()) {
            throw new IllegalArgumentException("La categoria es obligatoria");
        }
        if (request.getEstado() == null) {
            throw new IllegalArgumentException("El estado es obligatorio");
        }
        if (!proceso.getNombre().equalsIgnoreCase(request.getNombre())
                && procesoRepository.existsByNombreAndEmpresaIdAndIdNot(request.getNombre(), empresaId, procesoId)) {
            throw new IllegalArgumentException("Ya existe un proceso con ese nombre en la empresa");
        }

        proceso.setNombre(request.getNombre());
        proceso.setDescripcion(request.getDescripcion());
        proceso.setCategoria(request.getCategoria());
        proceso.setEstado(request.getEstado());
        proceso = procesoRepository.save(proceso);

        registrarHistorial(proceso, "Se edito el proceso '" + proceso.getNombre() + "' (usuario: " + usuario.getNombre() + ")");

        return toResponseDTO(proceso);
    }

    @Override
    @Transactional
    public void eliminarProceso(Long procesoId, Long empresaId, Long usuarioId, RolUsuario rol) {
        if (rol != RolUsuario.ADMINISTRADOR) {
            throw new PermisoDenegadoException("Solo un administrador puede eliminar procesos");
        }

        Proceso proceso = procesoRepository.findByIdAndEmpresaId(procesoId, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("El proceso no existe"));
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("El usuario no existe"));

        proceso.setActivo(false);
        procesoRepository.save(proceso);

        registrarHistorial(proceso, "Se elimino (logicamente) el proceso '" + proceso.getNombre() + "' (usuario: " + usuario.getNombre() + ")");
    }

    @Override
    public Page<ProcesoListItemDTO> listarProcesos(Long empresaId, String nombre, EstadoProceso estado,
                                                     String categoria, boolean incluirInactivos, Pageable pageable) {
        return procesoRepository.buscar(empresaId, incluirInactivos, nombre, estado, categoria, pageable)
                .map(p -> new ProcesoListItemDTO(p.getId(), p.getNombre(), p.getCategoria(), p.getEstado(), p.getActivo()));
    }

    @Override
    public ProcesoDetalleDTO obtenerDetalle(Long procesoId, Long empresaId) {
        Proceso proceso = procesoRepository.findByIdAndEmpresaId(procesoId, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("El proceso no existe"));

        List<LaneDTO> lanes = laneService.listarPorProceso(procesoId);
        List<ActividadDTO> actividades = actividadService.listarPorProceso(procesoId);

        return new ProcesoDetalleDTO(
                proceso.getId(), proceso.getNombre(), proceso.getDescripcion(), proceso.getCategoria(),
                proceso.getEstado(), proceso.getActivo(), lanes, actividades);
    }

    @Override
    public List<HistorialProcesoDTO> obtenerHistorial(Long procesoId, Long empresaId) {
        procesoRepository.findByIdAndEmpresaId(procesoId, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("El proceso no existe"));

        return historialProcesoRepository.findByProcesoIdOrderByFechaDesc(procesoId).stream()
                .map(h -> new HistorialProcesoDTO(h.getId(), h.getDescripcion(), h.getFecha()))
                .collect(Collectors.toList());
    }

    private void registrarHistorial(Proceso proceso, String descripcion) {
        historialProcesoRepository.save(new HistorialProceso(proceso, descripcion, LocalDateTime.now()));
    }

    private ProcesoResponseDTO toResponseDTO(Proceso proceso) {
        return new ProcesoResponseDTO(
                proceso.getId(), proceso.getNombre(), proceso.getDescripcion(), proceso.getCategoria(),
                proceso.getEstado(), proceso.getActivo(), proceso.getEmpresa().getId());
    }
}
