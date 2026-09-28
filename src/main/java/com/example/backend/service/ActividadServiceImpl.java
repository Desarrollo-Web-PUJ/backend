package com.example.backend.service;

import com.example.backend.dto.ActividadDTO;
import com.example.backend.dto.ActividadRequestDTO;
import com.example.backend.dto.EliminacionActividadResponseDTO;
import com.example.backend.entity.Actividad;
import com.example.backend.entity.Arco;
import com.example.backend.entity.HistorialProceso;
import com.example.backend.entity.Lane;
import com.example.backend.entity.Pool;
import com.example.backend.entity.Proceso;
import com.example.backend.entity.RolUsuario;
import com.example.backend.entity.TipoPool;
import com.example.backend.entity.TipoNodo;
import com.example.backend.exception.OperacionInvalidaException;
import com.example.backend.exception.PermisoDenegadoException;
import com.example.backend.exception.RecursoDuplicadoException;
import com.example.backend.exception.RecursoNoEncontradoException;
import com.example.backend.repository.ActividadRepository;
import com.example.backend.repository.ArcoRepository;
import com.example.backend.repository.HistorialProcesoRepository;
import com.example.backend.repository.LaneRepository;
import com.example.backend.repository.ProcesoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ActividadServiceImpl implements ActividadService {

    private final ActividadRepository actividadRepository;
    private final LaneRepository laneRepository;
    private final ProcesoRepository procesoRepository;
    private final HistorialProcesoRepository historialProcesoRepository;
    private final ArcoRepository arcoRepository;

    public ActividadServiceImpl(
            ActividadRepository actividadRepository,
            LaneRepository laneRepository,
            ProcesoRepository procesoRepository,
            HistorialProcesoRepository historialProcesoRepository,
            ArcoRepository arcoRepository) {

        this.actividadRepository = actividadRepository;
        this.laneRepository = laneRepository;
        this.procesoRepository = procesoRepository;
        this.historialProcesoRepository = historialProcesoRepository;
        this.arcoRepository = arcoRepository;
    }

    @Override
    @Transactional
    public ActividadDTO crearActividad(
            Long procesoId,
            ActividadRequestDTO request,
            Long empresaId,
            RolUsuario rol) {

        validarPermisoEdicion(rol);

        Proceso proceso = obtenerProceso(procesoId, empresaId);

        validarProcesoActivo(proceso);
        validarDatosActividad(request);

        if (actividadRepository.existsByNombreAndProcesoId(
                request.getNombre(),
                procesoId)) {

            throw new RecursoDuplicadoException(
                    "Ya existe una actividad con ese nombre en este proceso"
            );
        }

        Lane lane = obtenerLaneDelProceso(
                request.getLaneId(),
                procesoId
        );

        Actividad actividad = new Actividad();

        actividad.setNombre(request.getNombre().trim());
        actividad.setTipo(request.getTipo());
        actividad.setProceso(proceso);
        actividad.setLane(lane);
        actividad.setPosicionX(request.getPosicionX());
        actividad.setPosicionY(request.getPosicionY());
        actividad.setActivo(true);

        actividad = actividadRepository.save(actividad);

        historialProcesoRepository.save(
                new HistorialProceso(
                        proceso,
                        "Se creó la actividad '"
                                + actividad.getNombre()
                                + "' en la lane '"
                                + lane.getNombre()
                                + "'",
                        LocalDateTime.now()
                )
        );

        return toDTO(actividad);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActividadDTO> listarPorProceso(
            Long procesoId,
            Long empresaId) {

        obtenerProceso(procesoId, empresaId);

        return actividadRepository
                .findByProcesoIdAndActivoTrue(procesoId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ActividadDTO editarActividad(
            Long procesoId,
            Long actividadId,
            ActividadRequestDTO request,
            Long empresaId,
            RolUsuario rol) {

        validarPermisoEdicion(rol);

        Proceso proceso = obtenerProceso(
                procesoId,
                empresaId
        );

        validarProcesoActivo(proceso);

        Actividad actividad = actividadRepository
                .findByIdAndProcesoIdAndActivoTrue(
                        actividadId,
                        procesoId
                )
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "La actividad no existe en este proceso"
                        )
                );

        validarDatosActividad(request);

        if (actividadRepository
                .existsByNombreAndProcesoIdAndIdNot(
                        request.getNombre(),
                        procesoId,
                        actividadId
                )) {

            throw new RecursoDuplicadoException(
                    "Ya existe una actividad con ese nombre en este proceso"
            );
        }

        Lane lane = obtenerLaneDelProceso(
                request.getLaneId(),
                procesoId
        );

        String nombreAnterior = actividad.getNombre();
        String laneAnterior = actividad.getLane().getNombre();

        actividad.setNombre(request.getNombre().trim());
        actividad.setTipo(request.getTipo());
        actividad.setLane(lane);
        actividad.setPosicionX(request.getPosicionX());
        actividad.setPosicionY(request.getPosicionY());

        actividad = actividadRepository.save(actividad);

        String descripcion =
                "Se editó la actividad #"
                        + actividad.getId()
                        + " ('"
                        + nombreAnterior
                        + "' -> '"
                        + actividad.getNombre()
                        + "')";

        if (!laneAnterior.equals(lane.getNombre())) {
            descripcion +=
                    " y cambió de lane '"
                            + laneAnterior
                            + "' a '"
                            + lane.getNombre()
                            + "'";
        }

        historialProcesoRepository.save(
                new HistorialProceso(
                        proceso,
                        descripcion,
                        LocalDateTime.now()
                )
        );

        return toDTO(actividad);
    }

    @Override
    @Transactional
    public EliminacionActividadResponseDTO eliminarActividad(
            Long procesoId,
            Long actividadId,
            Long empresaId,
            RolUsuario rol) {

        if (rol != RolUsuario.ADMINISTRADOR) {
            throw new PermisoDenegadoException(
                    "Solo un administrador puede eliminar actividades"
            );
        }

        Proceso proceso = obtenerProceso(
                procesoId,
                empresaId
        );

        validarProcesoActivo(proceso);

        Actividad actividad = actividadRepository
                .findByIdAndProcesoIdAndActivoTrue(
                        actividadId,
                        procesoId
                )
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "La actividad no existe en este proceso"
                        )
                );

        List<Arco> arcosConectados = arcoRepository
                .findByProcesoId(procesoId)
                .stream()
                .filter(arco ->
                        (arco.getTipoOrigen() == TipoNodo.ACTIVIDAD
                                && actividadId.equals(arco.getOrigenId()))
                                ||
                        (arco.getTipoDestino() == TipoNodo.ACTIVIDAD
                                && actividadId.equals(arco.getDestinoId()))
                )
                .collect(Collectors.toList());

        Set<String> advertenciasSet =
                new LinkedHashSet<>();

        for (Arco arco : arcosConectados) {

            if (arco.getTipoOrigen() == TipoNodo.ACTIVIDAD
                    && actividadId.equals(arco.getOrigenId())) {

                long entradasDestino =
                        arcoRepository
                                .countByProcesoIdAndTipoDestinoAndDestinoId(
                                        procesoId,
                                        arco.getTipoDestino(),
                                        arco.getDestinoId()
                                );

                long entradasQueSeEliminaran =
                        arcosConectados
                                .stream()
                                .filter(a ->
                                        a.getTipoDestino() == arco.getTipoDestino()
                                                &&
                                        a.getDestinoId().equals(arco.getDestinoId())
                                )
                                .count();

                if (entradasDestino == entradasQueSeEliminaran) {
                    advertenciasSet.add(
                            "El nodo "
                                    + arco.getTipoDestino()
                                    + " "
                                    + arco.getDestinoId()
                                    + " quedará sin camino de entrada"
                    );
                }
            }

            if (arco.getTipoDestino() == TipoNodo.ACTIVIDAD
                    && actividadId.equals(arco.getDestinoId())) {

                long salidasOrigen =
                        arcoRepository
                                .countByProcesoIdAndTipoOrigenAndOrigenId(
                                        procesoId,
                                        arco.getTipoOrigen(),
                                        arco.getOrigenId()
                                );

                long salidasQueSeEliminaran =
                        arcosConectados
                                .stream()
                                .filter(a ->
                                        a.getTipoOrigen() == arco.getTipoOrigen()
                                                &&
                                        a.getOrigenId().equals(arco.getOrigenId())
                                )
                                .count();

                if (salidasOrigen == salidasQueSeEliminaran) {
                    advertenciasSet.add(
                            "El nodo "
                                    + arco.getTipoOrigen()
                                    + " "
                                    + arco.getOrigenId()
                                    + " quedará sin camino de salida"
                    );
                }
            }
        }

        if (!arcosConectados.isEmpty()) {
            arcoRepository.deleteAll(arcosConectados);

            advertenciasSet.add(
                    "Se eliminaron "
                            + arcosConectados.size()
                            + " arco(s) conectado(s) a la actividad"
            );
        }

        actividad.setActivo(false);
        actividadRepository.save(actividad);

        historialProcesoRepository.save(
                new HistorialProceso(
                        proceso,
                        "Se eliminó la actividad #"
                                + actividad.getId()
                                + " '"
                                + actividad.getNombre()
                                + "'"
                                + (
                                arcosConectados.isEmpty()
                                        ? ""
                                        : " junto con "
                                        + arcosConectados.size()
                                        + " arco(s) conectado(s)"
                        ),
                        LocalDateTime.now()
                )
        );

        List<String> advertencias =
                new ArrayList<>(advertenciasSet);

        return new EliminacionActividadResponseDTO(
                "Actividad eliminada correctamente",
                advertencias
        );
    }

    private Proceso obtenerProceso(
            Long procesoId,
            Long empresaId) {

        if (empresaId == null) {
            throw new PermisoDenegadoException(
                    "No hay una empresa asociada a la sesión"
            );
        }

        return procesoRepository
                .findByIdAndEmpresaId(
                        procesoId,
                        empresaId
                )
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "El proceso no existe o no pertenece a la empresa"
                        )
                );
    }

    private void validarProcesoActivo(
            Proceso proceso) {

        if (Boolean.FALSE.equals(proceso.getActivo())) {
            throw new OperacionInvalidaException(
                    "No se pueden modificar actividades de un proceso eliminado"
            );
        }
    }

    private void validarPermisoEdicion(
            RolUsuario rol) {

        if (rol != RolUsuario.ADMINISTRADOR
                && rol != RolUsuario.EDITOR) {

            throw new PermisoDenegadoException(
                    "No tiene permisos para editar actividades"
            );
        }
    }

    private void validarDatosActividad(
            ActividadRequestDTO request) {

        if (request == null) {
            throw new OperacionInvalidaException(
                    "Los datos de la actividad son obligatorios"
            );
        }

        if (request.getNombre() == null
                || request.getNombre().isBlank()) {

            throw new OperacionInvalidaException(
                    "El nombre de la actividad es obligatorio"
            );
        }

        if (request.getTipo() == null) {
            throw new OperacionInvalidaException(
                    "El tipo de actividad es obligatorio"
            );
        }

        if (request.getLaneId() == null) {
            throw new OperacionInvalidaException(
                    "Debe seleccionar la lane responsable"
            );
        }
    }

    private Lane obtenerLaneDelProceso(
            Long laneId,
            Long procesoId) {

        Lane lane = laneRepository
                .findById(laneId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "La lane no existe"
                        )
                );

        if (!lane.getProceso().getId().equals(procesoId)) {
            throw new OperacionInvalidaException(
                    "La lane seleccionada no pertenece a este proceso"
            );
        }

        if (!Boolean.TRUE.equals(lane.getActivo())) {
            throw new OperacionInvalidaException(
                    "La lane seleccionada está eliminada"
            );
        }

        if (lane.getRolProceso() == null) {
            throw new OperacionInvalidaException(
                    "La lane seleccionada no tiene rol de proceso asociado"
            );
        }

        Pool pool = lane.getPool();

        if (pool == null || !Boolean.TRUE.equals(pool.getActivo())) {
            throw new OperacionInvalidaException(
                    "La lane seleccionada no pertenece a un pool activo"
            );
        }

        if (pool.getTipo() == TipoPool.EXTERNO) {
            throw new OperacionInvalidaException(
                    "No se pueden crear actividades dentro de un pool externo"
            );
        }

        return lane;
    }

    private ActividadDTO toDTO(
            Actividad actividad) {

        ActividadDTO dto = new ActividadDTO();

        dto.setId(actividad.getId());
        dto.setNombre(actividad.getNombre());
        dto.setTipo(actividad.getTipo());
        dto.setProcesoId(actividad.getProceso().getId());
        dto.setLaneId(actividad.getLane().getId());
        dto.setLaneNombre(actividad.getLane().getNombre());
        if (actividad.getLane().getPool() != null) {
            dto.setPoolId(actividad.getLane().getPool().getId());
        }
        dto.setPosicionX(actividad.getPosicionX());
        dto.setPosicionY(actividad.getPosicionY());

        return dto;
    }
}
