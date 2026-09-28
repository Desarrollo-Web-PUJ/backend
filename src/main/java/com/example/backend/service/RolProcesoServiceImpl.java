package com.example.backend.service;

import com.example.backend.dto.EliminacionRolProcesoResponseDTO;
import com.example.backend.dto.LaneDTO;
import com.example.backend.dto.ProcesoUsoDTO;
import com.example.backend.dto.RolProcesoListItemDTO;
import com.example.backend.dto.RolProcesoRequestDTO;
import com.example.backend.dto.RolProcesoResponseDTO;
import com.example.backend.entity.Empresa;
import com.example.backend.entity.HistorialProceso;
import com.example.backend.entity.Lane;
import com.example.backend.entity.Proceso;
import com.example.backend.entity.RolProceso;
import com.example.backend.entity.RolUsuario;
import com.example.backend.exception.OperacionInvalidaException;
import com.example.backend.exception.PermisoDenegadoException;
import com.example.backend.exception.RecursoDuplicadoException;
import com.example.backend.exception.RecursoNoEncontradoException;
import com.example.backend.repository.EmpresaRepository;
import com.example.backend.repository.HistorialProcesoRepository;
import com.example.backend.repository.LaneRepository;
import com.example.backend.repository.ProcesoRepository;
import com.example.backend.repository.RolProcesoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class RolProcesoServiceImpl implements RolProcesoService {

    private static final int LONGITUD_MAXIMA_NOMBRE = 80;
    private static final int LONGITUD_MAXIMA_DESCRIPCION = 500;

    private final RolProcesoRepository rolProcesoRepository;
    private final EmpresaRepository empresaRepository;
    private final LaneRepository laneRepository;
    private final ProcesoRepository procesoRepository;
    private final HistorialProcesoRepository historialProcesoRepository;

    public RolProcesoServiceImpl(
            RolProcesoRepository rolProcesoRepository,
            EmpresaRepository empresaRepository,
            LaneRepository laneRepository,
            ProcesoRepository procesoRepository,
            HistorialProcesoRepository historialProcesoRepository) {

        this.rolProcesoRepository = rolProcesoRepository;
        this.empresaRepository = empresaRepository;
        this.laneRepository = laneRepository;
        this.procesoRepository = procesoRepository;
        this.historialProcesoRepository = historialProcesoRepository;
    }

    // HU-17
    @Override
    @Transactional
    public RolProcesoResponseDTO crear(
            RolProcesoRequestDTO request,
            Long empresaId,
            RolUsuario rol) {

        validarAdministrador(rol, "crear");

        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "La empresa de la sesión no existe"
                        )
                );

        String nombre = normalizarNombre(request.getNombre());
        String descripcion = normalizarDescripcion(request.getDescripcion());

        if (rolProcesoRepository
                .existsByEmpresaIdAndNombreIgnoreCaseAndActivoTrue(
                        empresaId,
                        nombre
                )) {

            throw new RecursoDuplicadoException(
                    "Ya existe un rol de proceso con ese nombre en la empresa"
            );
        }

        RolProceso rolProceso = new RolProceso();
        rolProceso.setNombre(nombre);
        rolProceso.setDescripcion(descripcion);
        rolProceso.setEmpresa(empresa);
        rolProceso.setActivo(true);

        rolProceso = rolProcesoRepository.save(rolProceso);

        return toDTO(rolProceso);
    }

    // HU-18
    @Override
    @Transactional
    public RolProcesoResponseDTO editar(
            Long rolProcesoId,
            RolProcesoRequestDTO request,
            Long empresaId,
            RolUsuario rol) {

        validarAdministrador(rol, "editar");

        RolProceso rolProceso = obtenerRolActivo(rolProcesoId, empresaId);

        String nombreNuevo = normalizarNombre(request.getNombre());
        String descripcionNueva = normalizarDescripcion(request.getDescripcion());

        if (rolProcesoRepository
                .existsByEmpresaIdAndNombreIgnoreCaseAndActivoTrueAndIdNot(
                        empresaId,
                        nombreNuevo,
                        rolProcesoId
                )) {

            throw new RecursoDuplicadoException(
                    "Ya existe un rol de proceso con ese nombre en la empresa"
            );
        }

        String nombreAnterior = rolProceso.getNombre();

        boolean cambioNombre = !nombreAnterior.equals(nombreNuevo);
        boolean cambioDescripcion =
                !Objects.equals(rolProceso.getDescripcion(), descripcionNueva);

        // Sin cambios reales: no se ensucia el historial
        if (!cambioNombre && !cambioDescripcion) {
            return toDTO(rolProceso);
        }

        rolProceso.setNombre(nombreNuevo);
        rolProceso.setDescripcion(descripcionNueva);
        rolProceso = rolProcesoRepository.save(rolProceso);

        String descripcionHistorial;

        if (cambioNombre) {
            descripcionHistorial =
                    "Se renombró el rol de proceso '"
                            + nombreAnterior
                            + "' a '"
                            + nombreNuevo
                            + "'"
                            + (cambioDescripcion
                            ? " y se actualizó su descripción"
                            : "");
        } else {
            descripcionHistorial =
                    "Se actualizó la descripción del rol de proceso '"
                            + nombreNuevo
                            + "'";
        }

        // Cada proceso que usa el rol registra el cambio en su historial
        List<Long> procesoIds = obtenerUso(List.of(rolProcesoId))
                .getOrDefault(rolProcesoId, List.of())
                .stream()
                .map(ProcesoUsoDTO::getId)
                .collect(Collectors.toList());

        if (!procesoIds.isEmpty()) {
            for (Proceso proceso : procesoRepository.findAllById(procesoIds)) {
                registrarHistorial(proceso, descripcionHistorial);
            }
        }

        return toDTO(rolProceso);
    }

    // HU-19
    @Override
    @Transactional
    public EliminacionRolProcesoResponseDTO eliminar(
            Long rolProcesoId,
            Long empresaId,
            RolUsuario rol) {

        validarAdministrador(rol, "eliminar");

        RolProceso rolProceso = obtenerRolActivo(rolProcesoId, empresaId);

        List<ProcesoUsoDTO> procesosEnUso = obtenerUso(List.of(rolProcesoId))
                .getOrDefault(rolProcesoId, List.of());

        if (!procesosEnUso.isEmpty()) {

            String nombres = procesosEnUso.stream()
                    .map(ProcesoUsoDTO::getNombre)
                    .collect(Collectors.joining(", "));

            throw new OperacionInvalidaException(
                    "No se puede eliminar el rol de proceso '"
                            + rolProceso.getNombre()
                            + "' porque está en uso en los procesos: "
                            + nombres
            );
        }

        rolProceso.setActivo(false);
        rolProcesoRepository.save(rolProceso);

        return new EliminacionRolProcesoResponseDTO(
                "Rol de proceso eliminado correctamente"
        );
    }

    // HU-20
    @Override
    @Transactional(readOnly = true)
    public Page<RolProcesoListItemDTO> consultar(
            String nombre,
            Pageable pageable,
            Long empresaId) {

        String filtro = nombre == null ? "" : nombre.trim();

        Page<RolProceso> pagina =
                rolProcesoRepository.buscar(empresaId, filtro, pageable);

        List<Long> ids = pagina.getContent().stream()
                .map(RolProceso::getId)
                .collect(Collectors.toList());

        Map<Long, List<ProcesoUsoDTO>> uso =
                ids.isEmpty() ? new HashMap<>() : obtenerUso(ids);

        return pagina.map(rolProceso -> {

            List<ProcesoUsoDTO> procesos =
                    uso.getOrDefault(rolProceso.getId(), List.of());

            boolean enUso = !procesos.isEmpty();

            return new RolProcesoListItemDTO(
                    rolProceso.getId(),
                    rolProceso.getNombre(),
                    rolProceso.getDescripcion(),
                    procesos,
                    enUso,
                    !enUso
            );
        });
    }

    // Nombra una lane de un proceso con un rol del catalogo de la empresa
    @Override
    @Transactional
    public LaneDTO crearLaneConRol(
            Long procesoId,
            Long rolProcesoId,
            Long empresaId,
            RolUsuario rol) {

        if (rol != RolUsuario.ADMINISTRADOR
                && rol != RolUsuario.EDITOR) {

            throw new PermisoDenegadoException(
                    "No tiene permisos para modificar las lanes del proceso"
            );
        }

        Proceso proceso = procesoRepository
                .findByIdAndEmpresaId(procesoId, empresaId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "El proceso no existe o no pertenece a la empresa"
                        )
                );

        if (Boolean.FALSE.equals(proceso.getActivo())) {
            throw new OperacionInvalidaException(
                    "No se pueden modificar las lanes de un proceso eliminado"
            );
        }

        RolProceso rolProceso = obtenerRolActivo(rolProcesoId, empresaId);

        if (laneRepository.existsByProcesoIdAndRolProcesoId(
                procesoId,
                rolProcesoId)) {

            throw new RecursoDuplicadoException(
                    "El proceso ya tiene una lane con el rol '"
                            + rolProceso.getNombre()
                            + "'"
            );
        }

        Lane lane = new Lane(rolProceso.getNombre(), proceso);
        lane.setRolProceso(rolProceso);
        lane = laneRepository.save(lane);

        registrarHistorial(
                proceso,
                "Se agregó la lane '"
                        + rolProceso.getNombre()
                        + "' (rol de proceso)"
        );

        return new LaneDTO(lane.getId(), lane.getNombre());
    }

    // Filas [rolId, procesoId, procesoNombre] agrupadas por rol
    private Map<Long, List<ProcesoUsoDTO>> obtenerUso(Collection<Long> rolIds) {

        Map<Long, List<ProcesoUsoDTO>> uso = new HashMap<>();

        for (Object[] fila : laneRepository.findUsoDeRoles(rolIds)) {

            Long rolId = ((Number) fila[0]).longValue();
            Long procesoId = ((Number) fila[1]).longValue();
            String procesoNombre = (String) fila[2];

            uso.computeIfAbsent(rolId, k -> new ArrayList<>())
                    .add(new ProcesoUsoDTO(procesoId, procesoNombre));
        }

        return uso;
    }

    private RolProceso obtenerRolActivo(Long rolProcesoId, Long empresaId) {

        return rolProcesoRepository
                .findByIdAndEmpresaIdAndActivoTrue(rolProcesoId, empresaId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "El rol de proceso no existe en la empresa"
                        )
                );
    }

    private void validarAdministrador(RolUsuario rol, String accion) {

        if (rol != RolUsuario.ADMINISTRADOR) {
            throw new PermisoDenegadoException(
                    "Solo un administrador puede " + accion + " roles de proceso"
            );
        }
    }

    private String normalizarNombre(String nombre) {

        if (nombre == null || nombre.isBlank()) {
            throw new OperacionInvalidaException(
                    "El nombre del rol de proceso es obligatorio"
            );
        }

        String limpio = nombre.trim();

        if (limpio.length() > LONGITUD_MAXIMA_NOMBRE) {
            throw new OperacionInvalidaException(
                    "El nombre del rol de proceso no puede superar los "
                            + LONGITUD_MAXIMA_NOMBRE
                            + " caracteres"
            );
        }

        return limpio;
    }

    private String normalizarDescripcion(String descripcion) {

        if (descripcion == null || descripcion.isBlank()) {
            return null;
        }

        String limpia = descripcion.trim();

        if (limpia.length() > LONGITUD_MAXIMA_DESCRIPCION) {
            throw new OperacionInvalidaException(
                    "La descripción del rol de proceso no puede superar los "
                            + LONGITUD_MAXIMA_DESCRIPCION
                            + " caracteres"
            );
        }

        return limpia;
    }

    private void registrarHistorial(Proceso proceso, String descripcion) {

        historialProcesoRepository.save(
                new HistorialProceso(
                        proceso,
                        descripcion,
                        LocalDateTime.now()
                )
        );
    }

    private RolProcesoResponseDTO toDTO(RolProceso rolProceso) {

        return new RolProcesoResponseDTO(
                rolProceso.getId(),
                rolProceso.getNombre(),
                rolProceso.getDescripcion()
        );
    }
}
