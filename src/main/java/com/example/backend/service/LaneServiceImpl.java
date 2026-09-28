package com.example.backend.service;

import com.example.backend.dto.EliminacionLaneResponseDTO;
import com.example.backend.dto.LaneDTO;
import com.example.backend.dto.LaneRequestDTO;
import com.example.backend.dto.ReordenarLanesRequestDTO;
import com.example.backend.entity.Lane;
import com.example.backend.entity.Pool;
import com.example.backend.entity.Proceso;
import com.example.backend.entity.RolProceso;
import com.example.backend.entity.RolUsuario;
import com.example.backend.entity.TipoPool;
import com.example.backend.exception.OperacionInvalidaException;
import com.example.backend.exception.PermisoDenegadoException;
import com.example.backend.exception.RecursoNoEncontradoException;
import com.example.backend.repository.ActividadRepository;
import com.example.backend.repository.EventoMensajeRepository;
import com.example.backend.repository.LaneRepository;
import com.example.backend.repository.PoolRepository;
import com.example.backend.repository.ProcesoRepository;
import com.example.backend.repository.RolProcesoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class LaneServiceImpl implements LaneService {

    private final LaneRepository laneRepository;
    private final ProcesoRepository procesoRepository;
    private final PoolRepository poolRepository;
    private final RolProcesoRepository rolProcesoRepository;
    private final ActividadRepository actividadRepository;
    private final EventoMensajeRepository eventoMensajeRepository;

    public LaneServiceImpl(
            LaneRepository laneRepository,
            ProcesoRepository procesoRepository,
            PoolRepository poolRepository,
            RolProcesoRepository rolProcesoRepository,
            ActividadRepository actividadRepository,
            EventoMensajeRepository eventoMensajeRepository) {
        this.laneRepository = laneRepository;
        this.procesoRepository = procesoRepository;
        this.poolRepository = poolRepository;
        this.rolProcesoRepository = rolProcesoRepository;
        this.actividadRepository = actividadRepository;
        this.eventoMensajeRepository = eventoMensajeRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LaneDTO> listarPorProceso(Long procesoId, Long empresaId) {
        obtenerProceso(procesoId, empresaId);

        return laneRepository.findByProcesoIdAndActivoTrueOrderByOrdenAsc(procesoId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LaneDTO> listarPorPool(Long procesoId, Long poolId, Long empresaId) {
        obtenerProceso(procesoId, empresaId);
        obtenerPool(poolId, procesoId);

        return laneRepository.findByPoolIdAndActivoTrueOrderByOrdenAsc(poolId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public LaneDTO crearLaneEnPool(Long procesoId, Long poolId, LaneRequestDTO request, Long empresaId, RolUsuario rol) {
        validarPermisoEdicion(rol);

        Proceso proceso = obtenerProceso(procesoId, empresaId);
        validarProcesoActivo(proceso);
        Pool pool = obtenerPool(poolId, procesoId);
        validarPoolPropio(pool);

        RolProceso rolProceso = obtenerRolProcesoActivo(request.getRolProcesoId(), empresaId);

        if (laneRepository.existsByPoolIdAndRolProcesoIdAndActivoTrue(poolId, rolProceso.getId())) {
            throw new OperacionInvalidaException("Ese rol de proceso ya tiene una lane en este pool");
        }

        Lane lane = new Lane();
        lane.setNombre(rolProceso.getNombre()); // se ignora si hay rolProceso, pero se deja consistente
        lane.setProceso(proceso);
        lane.setPool(pool);
        lane.setRolProceso(rolProceso);
        lane.setOrden(0);
        lane.setActivo(true);

        lane = laneRepository.save(lane);
        normalizarOrden(poolId, lane.getId(), request.getOrden());

        return toDTO(lane);
    }

    @Override
    @Transactional
    public LaneDTO editarLane(Long procesoId, Long poolId, Long laneId, LaneRequestDTO request, Long empresaId, RolUsuario rol) {
        validarPermisoEdicion(rol);

        Proceso proceso = obtenerProceso(procesoId, empresaId);
        validarProcesoActivo(proceso);
        Pool pool = obtenerPool(poolId, procesoId);
        validarPoolPropio(pool);

        Lane lane = laneRepository.findByIdAndPoolIdAndActivoTrue(laneId, poolId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la lane indicada en este pool"));

        RolProceso rolProceso = obtenerRolProcesoActivo(request.getRolProcesoId(), empresaId);

        Long rolActualId = lane.getRolProceso() != null ? lane.getRolProceso().getId() : null;
        boolean rolUsadoPorOtraLane = laneRepository.existsByPoolIdAndRolProcesoIdAndActivoTrue(poolId, rolProceso.getId())
                && !rolProceso.getId().equals(rolActualId);
        if (rolUsadoPorOtraLane) {
            throw new OperacionInvalidaException("Ese rol de proceso ya tiene una lane en este pool");
        }

        lane.setRolProceso(rolProceso);
        lane = laneRepository.save(lane);

        if (request.getOrden() != null) {
            normalizarOrden(poolId, lane.getId(), request.getOrden());
        }

        return toDTO(lane);
    }

    @Override
    @Transactional
    public List<LaneDTO> reordenarLanes(Long procesoId, Long poolId, ReordenarLanesRequestDTO request, Long empresaId, RolUsuario rol) {
        validarPermisoEdicion(rol);

        Proceso proceso = obtenerProceso(procesoId, empresaId);
        validarProcesoActivo(proceso);
        Pool pool = obtenerPool(poolId, procesoId);
        validarPoolPropio(pool);

        List<Lane> lanes = laneRepository.findByPoolIdAndActivoTrueOrderByOrdenAsc(pool.getId());

        if (lanes.size() != request.getLaneIdsEnOrden().size()) {
            throw new OperacionInvalidaException("Debe indicar el orden de todas las lanes del pool");
        }

        Set<Long> idsUnicos = new HashSet<>(request.getLaneIdsEnOrden());
        if (idsUnicos.size() != request.getLaneIdsEnOrden().size()) {
            throw new OperacionInvalidaException("El orden de lanes no puede contener IDs repetidos");
        }

        for (int i = 0; i < request.getLaneIdsEnOrden().size(); i++) {
            Long laneId = request.getLaneIdsEnOrden().get(i);
            Lane lane = lanes.stream()
                    .filter(l -> l.getId().equals(laneId))
                    .findFirst()
                    .orElseThrow(() -> new OperacionInvalidaException(
                            "La lane " + laneId + " no pertenece a este pool"));
            lane.setOrden(i);
            laneRepository.save(lane);
        }

        return laneRepository.findByPoolIdAndActivoTrueOrderByOrdenAsc(pool.getId()).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EliminacionLaneResponseDTO eliminarLane(Long procesoId, Long poolId, Long laneId, Long empresaId, RolUsuario rol) {
        validarPermisoEdicion(rol);

        Proceso proceso = obtenerProceso(procesoId, empresaId);
        validarProcesoActivo(proceso);
        obtenerPool(poolId, procesoId);

        Lane lane = laneRepository.findByIdAndPoolIdAndActivoTrue(laneId, poolId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la lane indicada en este pool"));

        boolean tieneActividades = actividadRepository.existsByLaneIdAndActivoTrue(laneId);
        if (tieneActividades) {
            throw new OperacionInvalidaException(
                    "No se puede eliminar la lane: reasigne sus actividades a otra lane primero");
        }

        boolean tieneEventos = eventoMensajeRepository.existsByLaneIdAndActivoTrue(laneId);
        if (tieneEventos) {
            throw new OperacionInvalidaException(
                    "No se puede eliminar la lane: reasigne o elimine sus eventos de mensaje primero");
        }

        lane.setActivo(false);
        laneRepository.save(lane);
        normalizarOrden(poolId, null, null);

        return new EliminacionLaneResponseDTO("Lane eliminada correctamente", List.of());
    }

    private LaneDTO toDTO(Lane lane) {
        LaneDTO dto = new LaneDTO(lane.getId(), lane.getNombre());
        dto.setProcesoId(lane.getProceso().getId());
        if (lane.getPool() != null) {
            dto.setPoolId(lane.getPool().getId());
        }
        if (lane.getRolProceso() != null) {
            dto.setRolProcesoId(lane.getRolProceso().getId());
        }
        dto.setOrden(lane.getOrden());
        return dto;
    }

    private RolProceso obtenerRolProcesoActivo(Long rolProcesoId, Long empresaId) {
        RolProceso rolProceso = rolProcesoRepository.findById(rolProcesoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("El rol de proceso indicado no existe"));

        if (!rolProceso.getEmpresa().getId().equals(empresaId)) {
            throw new PermisoDenegadoException("El rol de proceso no pertenece a esta empresa");
        }
        if (!Boolean.TRUE.equals(rolProceso.getActivo())) {
            throw new OperacionInvalidaException("El rol de proceso indicado está eliminado");
        }
        return rolProceso;
    }

    private Pool obtenerPool(Long poolId, Long procesoId) {
        return poolRepository.findByIdAndProcesoId(poolId, procesoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el pool indicado en este proceso"));
    }

    private void validarPoolPropio(Pool pool) {
        if (pool.getTipo() == TipoPool.EXTERNO) {
            throw new OperacionInvalidaException(
                    "Un pool externo (caja negra) no puede tener lanes ni elementos internos");
        }
    }

    private Proceso obtenerProceso(Long procesoId, Long empresaId) {
        return procesoRepository.findByIdAndEmpresaId(procesoId, empresaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("El proceso no existe en la empresa indicada"));
    }

    private void validarProcesoActivo(Proceso proceso) {
        if (!Boolean.TRUE.equals(proceso.getActivo())) {
            throw new OperacionInvalidaException("No se pueden modificar las lanes de un proceso eliminado");
        }
    }

    private void validarPermisoEdicion(RolUsuario rol) {
        if (rol != RolUsuario.ADMINISTRADOR && rol != RolUsuario.EDITOR) {
            throw new PermisoDenegadoException("El usuario no tiene permisos de edición");
        }
    }

    private void normalizarOrden(Long poolId, Long laneMovidaId, Integer ordenSolicitado) {
        List<Lane> lanes = new ArrayList<>(laneRepository.findByPoolIdAndActivoTrueOrderByOrdenAsc(poolId));

        Lane laneMovida = null;
        if (laneMovidaId != null) {
            laneMovida = lanes.stream()
                    .filter(lane -> lane.getId().equals(laneMovidaId))
                    .findFirst()
                    .orElseThrow(() -> new RecursoNoEncontradoException("No existe la lane indicada en este pool"));
            lanes.remove(laneMovida);
        }

        if (laneMovida != null) {
            int indice = ordenSolicitado == null ? lanes.size() : Math.max(0, Math.min(ordenSolicitado, lanes.size()));
            lanes.add(indice, laneMovida);
        }

        for (int i = 0; i < lanes.size(); i++) {
            Lane lane = lanes.get(i);
            if (lane.getOrden() == null || lane.getOrden() != i) {
                lane.setOrden(i);
                laneRepository.save(lane);
            }
        }
    }
}
