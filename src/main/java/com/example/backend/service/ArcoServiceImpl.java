package com.example.backend.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.dto.ArcoRequestDTO;
import com.example.backend.dto.ArcoResponseDTO;
import com.example.backend.dto.EliminacionArcoResponseDTO;
import com.example.backend.entity.Actividad;
import com.example.backend.entity.Arco;
import com.example.backend.entity.Gateway;
import com.example.backend.entity.HistorialProceso;
import com.example.backend.entity.Pool;
import com.example.backend.entity.Proceso;
import com.example.backend.entity.RolUsuario;
import com.example.backend.entity.TipoGateway;
import com.example.backend.entity.TipoNodo;
import com.example.backend.exception.OperacionInvalidaException;
import com.example.backend.exception.PermisoDenegadoException;
import com.example.backend.exception.RecursoDuplicadoException;
import com.example.backend.exception.RecursoNoEncontradoException;
import com.example.backend.repository.ActividadRepository;
import com.example.backend.repository.ArcoRepository;
import com.example.backend.repository.GatewayRepository;
import com.example.backend.repository.HistorialProcesoRepository;
import com.example.backend.repository.LaneRepository;
import com.example.backend.repository.PoolRepository;
import com.example.backend.repository.ProcesoRepository;

@Service
public class ArcoServiceImpl implements ArcoService {

    private final ArcoRepository arcoRepository;
    private final ProcesoRepository procesoRepository;
    private final ActividadRepository actividadRepository;
    private final GatewayRepository gatewayRepository;
    private final HistorialProcesoRepository historialProcesoRepository;
    private final LaneRepository laneRepository;
    private final PoolRepository poolRepository;

    public ArcoServiceImpl(
            ArcoRepository arcoRepository,
            ProcesoRepository procesoRepository,
            ActividadRepository actividadRepository,
            GatewayRepository gatewayRepository,
            HistorialProcesoRepository historialProcesoRepository,
            LaneRepository laneRepository,
            PoolRepository poolRepository) {

        this.arcoRepository = arcoRepository;
        this.procesoRepository = procesoRepository;
        this.actividadRepository = actividadRepository;
        this.gatewayRepository = gatewayRepository;
        this.historialProcesoRepository = historialProcesoRepository;
        this.laneRepository = laneRepository;
        this.poolRepository = poolRepository;
    }

    @Override
    @Transactional
    public ArcoResponseDTO crearArco(
            Long procesoId,
            ArcoRequestDTO request,
            Long empresaId,
            RolUsuario rol) {

        validarPermisoEdicion(rol);

        Proceso proceso = obtenerProceso(procesoId, empresaId);
        validarProcesoActivo(proceso);

        validarArco(procesoId, request, null);

        String condicion = resolverCondicionGateway(procesoId, request);

        Arco arco = new Arco();

        arco.setProceso(proceso);
        arco.setTipoOrigen(request.getTipoOrigen());
        arco.setOrigenId(request.getOrigenId());
        arco.setTipoDestino(request.getTipoDestino());
        arco.setDestinoId(request.getDestinoId());
        arco.setEtiqueta(request.getEtiqueta());
        arco.setCondicion(condicion);

        arco = arcoRepository.save(arco);

        historialProcesoRepository.save(
                new HistorialProceso(
                        proceso,
                        "Se creó el arco #" + arco.getId()
                                + " desde " + arco.getTipoOrigen()
                                + " " + arco.getOrigenId()
                                + " hacia " + arco.getTipoDestino()
                                + " " + arco.getDestinoId(),
                        LocalDateTime.now()
                )
        );

        return convertirDTO(arco);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArcoResponseDTO> listarPorProceso(
            Long procesoId,
            Long empresaId) {

        obtenerProceso(procesoId, empresaId);

        return arcoRepository
                .findByProcesoId(procesoId)
                .stream()
                .map(this::convertirDTO)
                .toList();
    }

    @Override
    @Transactional
    public ArcoResponseDTO editarArco(
            Long procesoId,
            Long arcoId,
            ArcoRequestDTO request,
            Long empresaId,
            RolUsuario rol) {

        validarPermisoEdicion(rol);

        Proceso proceso = obtenerProceso(procesoId, empresaId);

        validarProcesoActivo(proceso);

        Arco arco = arcoRepository
                .findByIdAndProcesoId(arcoId, procesoId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "No existe el arco indicado en este proceso"
                        )
                );

        validarArco(procesoId, request, arcoId);

        String condicion = resolverCondicionGateway(procesoId, request);

        arco.setTipoOrigen(request.getTipoOrigen());
        arco.setOrigenId(request.getOrigenId());
        arco.setTipoDestino(request.getTipoDestino());
        arco.setDestinoId(request.getDestinoId());
        arco.setEtiqueta(request.getEtiqueta());
        arco.setCondicion(condicion);

        arco = arcoRepository.save(arco);

        historialProcesoRepository.save(
                new HistorialProceso(
                        proceso,
                        "Se editó el arco #" + arco.getId(),
                        LocalDateTime.now()
                )
        );

        return convertirDTO(arco);
    }

    @Override
    @Transactional
    public EliminacionArcoResponseDTO eliminarArco(
            Long procesoId,
            Long arcoId,
            Long empresaId,
            RolUsuario rol) {

        if (rol != RolUsuario.ADMINISTRADOR) {
            throw new PermisoDenegadoException(
                    "Solo un administrador puede eliminar arcos"
            );
        }

        Proceso proceso = obtenerProceso(procesoId, empresaId);

        validarProcesoActivo(proceso);

        Arco arco = arcoRepository
                .findByIdAndProcesoId(arcoId, procesoId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "No existe el arco indicado en este proceso"
                        )
                );

        List<String> advertencias = new ArrayList<>();

        long salidasOrigen = arcoRepository
                .countByProcesoIdAndTipoOrigenAndOrigenId(
                        procesoId, arco.getTipoOrigen(), arco.getOrigenId());

        long entradasDestino = arcoRepository
                .countByProcesoIdAndTipoDestinoAndDestinoId(
                        procesoId, arco.getTipoDestino(), arco.getDestinoId());

        if (salidasOrigen == 1) {
            advertencias.add("El nodo de origen quedará sin camino de salida");
        }

        if (entradasDestino == 1) {
            advertencias.add("El nodo de destino quedará sin camino de entrada");
        }

        String descripcion =
                "Se eliminó el arco #" + arco.getId()
                        + " desde " + arco.getTipoOrigen()
                        + " " + arco.getOrigenId()
                        + " hacia " + arco.getTipoDestino()
                        + " " + arco.getDestinoId();

        arcoRepository.delete(arco);

        historialProcesoRepository.save(
                new HistorialProceso(proceso, descripcion, LocalDateTime.now())
        );

        return new EliminacionArcoResponseDTO("Arco eliminado correctamente", advertencias);
    }

    private void validarArco(
            Long procesoId,
            ArcoRequestDTO request,
            Long arcoIdActual) {

        if (request == null) {
            throw new OperacionInvalidaException("Los datos del arco son obligatorios");
        }

        if (request.getTipoOrigen() == null
                || request.getTipoDestino() == null
                || request.getOrigenId() == null
                || request.getDestinoId() == null) {

            throw new OperacionInvalidaException(
                    "Debe indicar el tipo y el ID de los nodos de origen y destino"
            );
        }

        if (request.getOrigenId() <= 0 || request.getDestinoId() <= 0) {
            throw new OperacionInvalidaException("Los IDs de los nodos deben ser positivos");
        }

        if (request.getTipoOrigen() == request.getTipoDestino()
                && request.getOrigenId().equals(request.getDestinoId())) {

            throw new OperacionInvalidaException(
                    "El origen y el destino no pueden ser el mismo nodo"
            );
        }

        validarNodo(request.getTipoOrigen(), request.getOrigenId(), procesoId);
        validarNodo(request.getTipoDestino(), request.getDestinoId(), procesoId);

        validarMismoPool(
                request.getTipoOrigen(), request.getOrigenId(),
                request.getTipoDestino(), request.getDestinoId()
        );

        boolean duplicado;

        if (arcoIdActual == null) {
            duplicado = arcoRepository
                    .existsByProcesoIdAndTipoOrigenAndOrigenIdAndTipoDestinoAndDestinoId(
                            procesoId,
                            request.getTipoOrigen(), request.getOrigenId(),
                            request.getTipoDestino(), request.getDestinoId());
        } else {
            duplicado = arcoRepository
                    .existsByProcesoIdAndTipoOrigenAndOrigenIdAndTipoDestinoAndDestinoIdAndIdNot(
                            procesoId,
                            request.getTipoOrigen(), request.getOrigenId(),
                            request.getTipoDestino(), request.getDestinoId(),
                            arcoIdActual);
        }

        if (duplicado) {
            throw new RecursoDuplicadoException("Ya existe un arco entre esos dos nodos");
        }
    }

    private void validarMismoPool(
            TipoNodo tipoOrigen, Long origenId,
            TipoNodo tipoDestino, Long destinoId) {

        Long poolOrigenId = resolverPoolDeNodo(tipoOrigen, origenId);
        Long poolDestinoId = resolverPoolDeNodo(tipoDestino, destinoId);

        if (poolOrigenId == null || poolDestinoId == null) {
            throw new OperacionInvalidaException(
                    "Todo nodo conectado por un arco debe pertenecer a un pool"
            );
        }

        if (!poolOrigenId.equals(poolDestinoId)) {
            throw new OperacionInvalidaException(
                    "El arco no puede conectar nodos de pools distintos. " +
                    "Para comunicar dos pools use un flujo de mensajes, no un arco."
            );
        }
    }

    private Long resolverPoolDeNodo(TipoNodo tipoNodo, Long nodoId) {

        if (tipoNodo == TipoNodo.ACTIVIDAD) {
            return actividadRepository.findById(nodoId)
                    .map(Actividad::getLane)
                    .map(lane -> lane.getPool())
                    .map(Pool::getId)
                    .orElse(null);
        }

        if (tipoNodo == TipoNodo.GATEWAY) {
            return gatewayRepository.findById(nodoId)
                    .map(Gateway::getPool)
                    .map(Pool::getId)
                    .orElse(null);
        }

        return null;
    }

    private void validarNodo(
            TipoNodo tipoNodo,
            Long nodoId,
            Long procesoId) {

        if (tipoNodo == TipoNodo.ACTIVIDAD) {

            actividadRepository
                    .findByIdAndProcesoIdAndActivoTrue(nodoId, procesoId)
                    .orElseThrow(() ->
                            new RecursoNoEncontradoException(
                                    "La actividad " + nodoId
                                            + " no existe, está eliminada o no pertenece al proceso"
                            )
                    );

            return;
        }

        if (tipoNodo == TipoNodo.GATEWAY) {
            obtenerGatewayActivo(nodoId, procesoId);
            return;
        }

        if (tipoNodo == TipoNodo.EVENTO) {
            throw new OperacionInvalidaException(
                    "La validación de eventos quedará disponible al integrar la entidad Evento"
            );
        }

        throw new OperacionInvalidaException("El tipo de nodo no está soportado");
    }

    private String resolverCondicionGateway(
            Long procesoId,
            ArcoRequestDTO request) {

        if (request.getTipoOrigen() != TipoNodo.GATEWAY) {
            return null;
        }

        Gateway gateway = obtenerGatewayActivo(request.getOrigenId(), procesoId);

        String condicion = request.getCondicion();

        if (gateway.getTipo() == TipoGateway.EXCLUSIVO
                || gateway.getTipo() == TipoGateway.INCLUSIVO) {

            if (condicion == null || condicion.isBlank()) {
                throw new OperacionInvalidaException(
                        "Los arcos salientes de un gateway "
                                + gateway.getTipo()
                                + " deben tener una condición"
                );
            }

            return condicion.trim();
        }

        if (gateway.getTipo() == TipoGateway.PARALELO) {

            if (condicion != null && !condicion.isBlank()) {
                throw new OperacionInvalidaException(
                        "Los arcos salientes de un gateway PARALELO no deben tener condición"
                );
            }

            return null;
        }

        throw new OperacionInvalidaException("El tipo de gateway no está soportado");
    }

    private Gateway obtenerGatewayActivo(
            Long gatewayId,
            Long procesoId) {

        return gatewayRepository
                .findByIdAndProcesoIdAndActivoTrue(gatewayId, procesoId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "El gateway " + gatewayId
                                        + " no existe, está eliminado o no pertenece al proceso"
                        )
                );
    }

    private Proceso obtenerProceso(
            Long procesoId,
            Long empresaId) {

        return procesoRepository
                .findByIdAndEmpresaId(procesoId, empresaId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "El proceso no existe en la empresa indicada"
                        )
                );
    }

    private void validarProcesoActivo(Proceso proceso) {
        if (!Boolean.TRUE.equals(proceso.getActivo())) {
            throw new OperacionInvalidaException(
                    "No se pueden modificar los arcos de un proceso eliminado"
            );
        }
    }

    private void validarPermisoEdicion(RolUsuario rol) {
        if (rol != RolUsuario.ADMINISTRADOR && rol != RolUsuario.EDITOR) {
            throw new PermisoDenegadoException("El usuario no tiene permisos de edición");
        }
    }

    private ArcoResponseDTO convertirDTO(Arco arco) {

        ArcoResponseDTO dto = new ArcoResponseDTO();

        dto.setId(arco.getId());
        dto.setProcesoId(arco.getProceso().getId());
        dto.setTipoOrigen(arco.getTipoOrigen());
        dto.setOrigenId(arco.getOrigenId());
        dto.setTipoDestino(arco.getTipoDestino());
        dto.setDestinoId(arco.getDestinoId());
        dto.setEtiqueta(arco.getEtiqueta());
        dto.setCondicion(arco.getCondicion());

        return dto;
    }
}
