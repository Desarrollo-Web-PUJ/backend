package com.example.backend.service;

import com.example.backend.dto.EliminacionPoolResponseDTO;
import com.example.backend.dto.PoolRequestDTO;
import com.example.backend.dto.PoolResponseDTO;
import com.example.backend.entity.HistorialProceso;
import com.example.backend.entity.Pool;
import com.example.backend.entity.Proceso;
import com.example.backend.entity.RolUsuario;
import com.example.backend.entity.TipoPool;
import com.example.backend.exception.OperacionInvalidaException;
import com.example.backend.exception.PermisoDenegadoException;
import com.example.backend.exception.RecursoDuplicadoException;
import com.example.backend.exception.RecursoNoEncontradoException;
import com.example.backend.repository.HistorialProcesoRepository;
import com.example.backend.repository.LaneRepository;
import com.example.backend.repository.PoolRepository;
import com.example.backend.repository.ProcesoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PoolServiceImpl implements PoolService {

    private final PoolRepository poolRepository;
    private final ProcesoRepository procesoRepository;
    private final LaneRepository laneRepository;
    private final HistorialProcesoRepository historialProcesoRepository;

    public PoolServiceImpl(
            PoolRepository poolRepository,
            ProcesoRepository procesoRepository,
            LaneRepository laneRepository,
            HistorialProcesoRepository historialProcesoRepository) {
        this.poolRepository = poolRepository;
        this.procesoRepository = procesoRepository;
        this.laneRepository = laneRepository;
        this.historialProcesoRepository = historialProcesoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PoolResponseDTO> listarPorProceso(Long procesoId, Long empresaId) {
        obtenerProceso(procesoId, empresaId);

        return poolRepository.findByProcesoIdAndActivoTrue(procesoId).stream()
                .map(this::convertirDTO)
                .toList();
    }

    @Override
    @Transactional
    public PoolResponseDTO crearPool(Long procesoId, PoolRequestDTO request, Long empresaId, RolUsuario rol) {
        validarPermisoEdicion(rol);

        Proceso proceso = obtenerProceso(procesoId, empresaId);
        validarProcesoActivo(proceso);
        validarRequest(request);

        if (poolRepository.existsByProcesoIdAndNombreIgnoreCaseAndActivoTrue(procesoId, request.getNombre())) {
            throw new RecursoDuplicadoException("Ya existe un pool con ese nombre en este proceso");
        }

        Pool pool = new Pool();
        pool.setNombre(request.getNombre().trim());
        pool.setTipo(request.getTipo());
        pool.setProceso(proceso);
        pool.setActivo(true);

        pool = poolRepository.save(pool);

        registrarHistorial(proceso, "Se creó el pool \"" + pool.getNombre() + "\" (" + pool.getTipo() + ")");

        return convertirDTO(pool);
    }

    @Override
    @Transactional
    public PoolResponseDTO editarPool(Long procesoId, Long poolId, PoolRequestDTO request, Long empresaId, RolUsuario rol) {
        validarPermisoEdicion(rol);

        Proceso proceso = obtenerProceso(procesoId, empresaId);
        validarProcesoActivo(proceso);
        validarRequest(request);

        Pool pool = obtenerPool(poolId, procesoId);

        if (poolRepository.existsByProcesoIdAndNombreIgnoreCaseAndIdNotAndActivoTrue(
                procesoId, request.getNombre(), poolId)) {
            throw new RecursoDuplicadoException("Ya existe otro pool con ese nombre en este proceso");
        }

        // Un pool EXTERNO es caja negra: no puede convertirse en PROPIO si ya tiene lanes,
        // ni al revés puede quedar con lanes si se marca EXTERNO.
        boolean tieneLanes = !laneRepository.findByPoolIdAndActivoTrueOrderByOrdenAsc(poolId).isEmpty();
        if (request.getTipo() == TipoPool.EXTERNO && tieneLanes) {
            throw new OperacionInvalidaException(
                    "No se puede marcar como externo (caja negra) un pool que ya tiene lanes definidas");
        }

        pool.setNombre(request.getNombre().trim());
        pool.setTipo(request.getTipo());

        pool = poolRepository.save(pool);

        registrarHistorial(proceso, "Se editó el pool #" + pool.getId() + " (\"" + pool.getNombre() + "\")");

        return convertirDTO(pool);
    }

    @Override
    @Transactional
    public EliminacionPoolResponseDTO eliminarPool(Long procesoId, Long poolId, Long empresaId, RolUsuario rol) {
        if (rol != RolUsuario.ADMINISTRADOR) {
            throw new PermisoDenegadoException("Solo un administrador puede eliminar pools");
        }

        Proceso proceso = obtenerProceso(procesoId, empresaId);
        validarProcesoActivo(proceso);

        Pool pool = obtenerPool(poolId, procesoId);

        boolean tieneLanes = !laneRepository.findByPoolIdAndActivoTrueOrderByOrdenAsc(poolId).isEmpty();
        if (tieneLanes) {
            throw new OperacionInvalidaException(
                    "No se puede eliminar el pool: primero elimine o reasigne sus lanes");
        }

        pool.setActivo(false);
        poolRepository.save(pool);

        registrarHistorial(proceso, "Se eliminó el pool #" + pool.getId() + " (\"" + pool.getNombre() + "\")");

        return new EliminacionPoolResponseDTO("Pool eliminado correctamente", List.of());
    }

    private void validarRequest(PoolRequestDTO request) {
        if (request == null || request.getNombre() == null || request.getNombre().isBlank()) {
            throw new OperacionInvalidaException("El nombre del pool es obligatorio");
        }
        if (request.getTipo() == null) {
            throw new OperacionInvalidaException("El tipo de pool es obligatorio");
        }
    }

    private Pool obtenerPool(Long poolId, Long procesoId) {
        return poolRepository.findByIdAndProcesoId(poolId, procesoId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe el pool indicado en este proceso"));
    }

    private Proceso obtenerProceso(Long procesoId, Long empresaId) {
        return procesoRepository.findByIdAndEmpresaId(procesoId, empresaId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El proceso no existe en la empresa indicada"));
    }

    private void validarProcesoActivo(Proceso proceso) {
        if (!Boolean.TRUE.equals(proceso.getActivo())) {
            throw new OperacionInvalidaException("No se pueden modificar los pools de un proceso eliminado");
        }
    }

    private void validarPermisoEdicion(RolUsuario rol) {
        if (rol != RolUsuario.ADMINISTRADOR && rol != RolUsuario.EDITOR) {
            throw new PermisoDenegadoException("El usuario no tiene permisos de edición");
        }
    }

    private void registrarHistorial(Proceso proceso, String descripcion) {
        historialProcesoRepository.save(new HistorialProceso(proceso, descripcion, LocalDateTime.now()));
    }

    private PoolResponseDTO convertirDTO(Pool pool) {
        PoolResponseDTO dto = new PoolResponseDTO();
        dto.setId(pool.getId());
        dto.setNombre(pool.getNombre());
        dto.setTipo(pool.getTipo());
        dto.setProcesoId(pool.getProceso().getId());
        dto.setCantidadLanes(laneRepository.findByPoolIdAndActivoTrueOrderByOrdenAsc(pool.getId()).size());
        return dto;
    }
}