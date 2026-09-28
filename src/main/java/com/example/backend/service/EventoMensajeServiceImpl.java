package com.example.backend.service;

import com.example.backend.dto.*;
import com.example.backend.entity.*;
import com.example.backend.exception.OperacionInvalidaException;
import com.example.backend.exception.PermisoDenegadoException;
import com.example.backend.exception.RecursoNoEncontradoException;
import com.example.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventoMensajeServiceImpl implements EventoMensajeService {

    private final EventoMensajeRepository eventoRepository;
    private final FlujoMensajeRepository flujoRepository;
    private final ProcesoRepository procesoRepository;
    private final PoolRepository poolRepository;
    private final LaneRepository laneRepository;
    private final HistorialProcesoRepository historialRepository;
    private final JsonMapper objectMapper;

    public EventoMensajeServiceImpl(
            EventoMensajeRepository eventoRepository,
            FlujoMensajeRepository flujoRepository,
            ProcesoRepository procesoRepository,
            PoolRepository poolRepository,
            LaneRepository laneRepository,
            HistorialProcesoRepository historialRepository,
            JsonMapper objectMapper) {
        this.eventoRepository = eventoRepository;
        this.flujoRepository = flujoRepository;
        this.procesoRepository = procesoRepository;
        this.poolRepository = poolRepository;
        this.laneRepository = laneRepository;
        this.historialRepository = historialRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<EventoMensajeResponseDTO> listarPorProceso(Long procesoId, Long empresaId) {
        verificarProceso(procesoId, empresaId);
        return eventoRepository.findByProcesoIdAndActivoTrue(procesoId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventoMensajeResponseDTO crearEvento(Long procesoId,
                                                 EventoMensajeRequestDTO request,
                                                 Long empresaId, RolUsuario rol) {
        verificarPermiso(rol);
        Proceso proceso = verificarProceso(procesoId, empresaId);
        Pool pool = verificarPool(procesoId, request.getPoolId());
        Lane lane = request.getLaneId() != null
                ? verificarLane(pool.getId(), request.getLaneId()) : null;

        EventoMensaje evento = new EventoMensaje();
        evento.setProceso(proceso);
        evento.setPool(pool);
        evento.setLane(lane);
        aplicarDatos(evento, request, procesoId);

        evento = eventoRepository.save(evento);

        List<String> advertencias = validarReglas(evento, procesoId);

        registrarHistorial(proceso,
                "Se creó evento de mensaje '" + evento.getNombreMensaje() +
                        "' (" + evento.getTipo() + ") en el pool '" + pool.getNombre() + "'");

        EventoMensajeResponseDTO dto = toDTO(evento);
        dto.setAdvertencias(advertencias);
        return dto;
    }

    @Override
    @Transactional
    public EventoMensajeResponseDTO editarEvento(Long procesoId, Long eventoId,
                                                  EventoMensajeRequestDTO request,
                                                  Long empresaId, RolUsuario rol) {
        verificarPermiso(rol);
        Proceso proceso = verificarProceso(procesoId, empresaId);
        EventoMensaje evento = eventoRepository.findByIdAndProcesoIdAndActivoTrue(eventoId, procesoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Evento de mensaje no encontrado"));

        Pool pool = verificarPool(procesoId, request.getPoolId());
        Lane lane = request.getLaneId() != null
                ? verificarLane(pool.getId(), request.getLaneId()) : null;

        evento.setPool(pool);
        evento.setLane(lane);
        aplicarDatos(evento, request, procesoId);

        evento = eventoRepository.save(evento);
        List<String> advertencias = validarReglas(evento, procesoId);

        registrarHistorial(proceso,
                "Se editó el evento de mensaje '" + evento.getNombreMensaje() + "'");

        EventoMensajeResponseDTO dto = toDTO(evento);
        dto.setAdvertencias(advertencias);
        return dto;
    }

    @Override
    @Transactional
    public EliminacionEventoMensajeResponseDTO eliminarEvento(Long procesoId, Long eventoId,
                                                               Long empresaId, RolUsuario rol) {
        verificarPermiso(rol);
        Proceso proceso = verificarProceso(procesoId, empresaId);
        EventoMensaje evento = eventoRepository.findByIdAndProcesoIdAndActivoTrue(eventoId, procesoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Evento de mensaje no encontrado"));

        List<String> advertencias = new ArrayList<>();
        long flujosOrigen = flujoRepository.countByOrigenIdAndActivoTrue(eventoId);
        long flujosDestino = flujoRepository.countByDestinoIdAndActivoTrue(eventoId);
        if (flujosOrigen > 0 || flujosDestino > 0) {
            advertencias.add("Se eliminarán también los flujos de mensaje asociados ("
                    + (flujosOrigen + flujosDestino) + ")");
            flujoRepository.findByProcesoIdAndActivoTrue(procesoId).stream()
                    .filter(f -> (f.getOrigen() != null && f.getOrigen().getId().equals(eventoId))
                            || (f.getDestino() != null && f.getDestino().getId().equals(eventoId)))
                    .forEach(f -> { f.setActivo(false); flujoRepository.save(f); });
        }

        evento.setActivo(false);
        eventoRepository.save(evento);

        registrarHistorial(proceso,
                "Se eliminó el evento de mensaje '" + evento.getNombreMensaje() + "'");

        return new EliminacionEventoMensajeResponseDTO(
                "Evento de mensaje eliminado correctamente", advertencias);
    }

    // ---------- Helpers ----------

    private void aplicarDatos(EventoMensaje e, EventoMensajeRequestDTO r, Long procesoId) {
        e.setTipo(r.getTipo());
        e.setNombreMensaje(r.getNombreMensaje());
        e.setClaveCorrelacion(r.getClaveCorrelacion());
        e.setCorrelacionDefinida(r.getClaveCorrelacion() != null && !r.getClaveCorrelacion().isBlank());
        e.setComportamientoSinCorrelacion(r.getComportamientoSinCorrelacion());
        e.setOrigenExterno(Boolean.TRUE.equals(r.getOrigenExterno()));
        e.setTipoDestinoExterno(r.getTipoDestinoExterno());
        e.setComportamientoFallo(r.getComportamientoFallo());
        e.setPosicionX(r.getPosicionX());
        e.setPosicionY(r.getPosicionY());
        try {
            e.setCamposMensaje(objectMapper.writeValueAsString(r.getCampos()));
            e.setActividadesConsumidoras(objectMapper.writeValueAsString(r.getActividadesConsumidoras()));
        } catch (Exception ex) {
            throw new OperacionInvalidaException("Error al serializar campos del mensaje");
        }
    }

    private List<String> validarReglas(EventoMensaje e, Long procesoId) {
        List<String> advertencias = new ArrayList<>();

        // HU-25: THROW debe tener un CATCH con el mismo nombre en OTRO pool
        if (e.getTipo() == TipoEventoMensaje.THROW) {
            List<EventoMensaje> catches = eventoRepository
                    .findByProcesoIdAndNombreMensajeAndTipoInAndActivoTrue(
                            procesoId,
                            e.getNombreMensaje(),
                            List.of(TipoEventoMensaje.CATCH_INICIO, TipoEventoMensaje.CATCH_INTERMEDIO));
            boolean hayReceptorEnOtroPool = catches.stream()
                    .anyMatch(c -> !c.getPool().getId().equals(e.getPool().getId()));
            if (!hayReceptorEnOtroPool) {
                advertencias.add("El mensaje '" + e.getNombreMensaje()
                        + "' queda sin receptor: no existe un Message Catch con ese nombre en otro pool.");
            }
        }

        // HU-27: CATCH_INTERMEDIO sin clave de correlación
        if (e.getTipo() == TipoEventoMensaje.CATCH_INTERMEDIO
                && (e.getClaveCorrelacion() == null || e.getClaveCorrelacion().isBlank())) {
            advertencias.add("El Message Catch intermedio '" + e.getNombreMensaje()
                    + "' no tiene clave de correlación definida.");
        }

        // HU-27: CATCH_INICIO sin THROW ni origen externo
        if (e.getTipo() == TipoEventoMensaje.CATCH_INICIO
                && !Boolean.TRUE.equals(e.getOrigenExterno())) {
            List<EventoMensaje> throws_ = eventoRepository
                    .findByProcesoIdAndNombreMensajeAndTipoInAndActivoTrue(
                            procesoId,
                            e.getNombreMensaje(),
                            List.of(TipoEventoMensaje.THROW));
            if (throws_.isEmpty()) {
                advertencias.add("El Message Catch de inicio '" + e.getNombreMensaje()
                        + "' no tiene un Message Throw correspondiente ni está marcado como origen externo.");
            }
        }

        // HU-28: ambigüedad nombre + clave de correlación
        if (e.getClaveCorrelacion() != null && !e.getClaveCorrelacion().isBlank()) {
            List<EventoMensaje> duplicados = eventoRepository
                    .findByProcesoIdAndNombreMensajeAndClaveCorrelacionAndActivoTrue(
                            procesoId, e.getNombreMensaje(), e.getClaveCorrelacion());
            long distintos = duplicados.stream()
                    .filter(x -> !x.getId().equals(e.getId()))
                    .count();
            if (distintos > 0) {
                advertencias.add("Existen otros mensajes con el mismo nombre '"
                        + e.getNombreMensaje() + "' y la misma clave de correlación '"
                        + e.getClaveCorrelacion() + "': el modelo es ambiguo.");
            }
        }

        // HU-26: THROW a pool externo debe tener tipoDestinoExterno y comportamientoFallo
        if (e.getTipo() == TipoEventoMensaje.THROW
                && e.getPool().getTipo() == TipoPool.EXTERNO) {
            if (e.getTipoDestinoExterno() == null) {
                throw new OperacionInvalidaException(
                        "Debe indicar el tipo de destino (correo, servicio web o cola)");
            }
            if (e.getComportamientoFallo() == null) {
                throw new OperacionInvalidaException(
                        "Debe indicar el comportamiento ante fallo de la notificación");
            }
        }

        return advertencias;
    }

    private void verificarPermiso(RolUsuario rol) {
        if (rol == RolUsuario.LECTURA) {
            throw new PermisoDenegadoException("No tiene permiso para modificar el modelo");
        }
    }

    private Proceso verificarProceso(Long procesoId, Long empresaId) {
        return procesoRepository.findByIdAndEmpresaId(procesoId, empresaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Proceso no encontrado"));
    }

    private Pool verificarPool(Long procesoId, Long poolId) {
        return poolRepository.findByIdAndProcesoId(poolId, procesoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pool no encontrado"));
    }

    private Lane verificarLane(Long poolId, Long laneId) {
        return laneRepository.findByIdAndPoolIdAndActivoTrue(laneId, poolId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Lane no encontrada en el pool"));
    }

    private void registrarHistorial(Proceso proceso, String descripcion) {
        historialRepository.save(new HistorialProceso(
                proceso, descripcion, LocalDateTime.now()));
    }

    private EventoMensajeResponseDTO toDTO(EventoMensaje e) {
        EventoMensajeResponseDTO dto = new EventoMensajeResponseDTO();
        dto.setId(e.getId());
        dto.setProcesoId(e.getProceso().getId());
        dto.setPoolId(e.getPool().getId());
        dto.setPoolNombre(e.getPool().getNombre());
        if (e.getLane() != null) {
            dto.setLaneId(e.getLane().getId());
            dto.setLaneNombre(e.getLane().getNombre());
        }
        dto.setTipo(e.getTipo());
        dto.setNombreMensaje(e.getNombreMensaje());
        dto.setClaveCorrelacion(e.getClaveCorrelacion());
        dto.setCorrelacionDefinida(e.getCorrelacionDefinida());
        dto.setComportamientoSinCorrelacion(e.getComportamientoSinCorrelacion());
        dto.setOrigenExterno(e.getOrigenExterno());
        dto.setTipoDestinoExterno(e.getTipoDestinoExterno());
        dto.setComportamientoFallo(e.getComportamientoFallo());
        dto.setPosicionX(e.getPosicionX());
        dto.setPosicionY(e.getPosicionY());

        try {
            if (e.getCamposMensaje() != null) {
                dto.setCampos(objectMapper.readValue(e.getCamposMensaje(),
                        new TypeReference<List<CampoMensajeDTO>>() {}));
            }
            if (e.getActividadesConsumidoras() != null) {
                dto.setActividadesConsumidoras(objectMapper.readValue(
                        e.getActividadesConsumidoras(),
                        new TypeReference<List<Long>>() {}));
            }
        } catch (Exception ignored) { }

        return dto;
    }
}
