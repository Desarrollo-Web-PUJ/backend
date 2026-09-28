package com.example.backend.service;

import com.example.backend.dto.*;
import com.example.backend.entity.*;
import com.example.backend.exception.OperacionInvalidaException;
import com.example.backend.exception.PermisoDenegadoException;
import com.example.backend.exception.RecursoNoEncontradoException;
import com.example.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FlujoMensajeServiceImpl implements FlujoMensajeService {

    private final FlujoMensajeRepository flujoRepository;
    private final EventoMensajeRepository eventoRepository;
    private final PoolRepository poolRepository;
    private final ProcesoRepository procesoRepository;
    private final HistorialProcesoRepository historialRepository;

    public FlujoMensajeServiceImpl(FlujoMensajeRepository flujoRepository,
                                    EventoMensajeRepository eventoRepository,
                                    PoolRepository poolRepository,
                                    ProcesoRepository procesoRepository,
                                    HistorialProcesoRepository historialRepository) {
        this.flujoRepository = flujoRepository;
        this.eventoRepository = eventoRepository;
        this.poolRepository = poolRepository;
        this.procesoRepository = procesoRepository;
        this.historialRepository = historialRepository;
    }

    @Override
    public List<FlujoMensajeResponseDTO> listarPorProceso(Long procesoId, Long empresaId) {
        verificarProceso(procesoId, empresaId);
        return flujoRepository.findByProcesoIdAndActivoTrue(procesoId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public FlujoMensajeResponseDTO crearFlujo(Long procesoId,
                                               FlujoMensajeRequestDTO request,
                                               Long empresaId, RolUsuario rol) {
        verificarPermiso(rol);
        Proceso proceso = verificarProceso(procesoId, empresaId);
        validarProcesoActivo(proceso);

        FlujoMensaje flujo = new FlujoMensaje();
        flujo.setProceso(proceso);
        List<String> advertencias = aplicarDatos(flujo, request, procesoId, null);

        flujo = flujoRepository.save(flujo);

        registrarHistorial(proceso,
                "Se creó un flujo de mensaje desde '" + flujo.getOrigen().getNombreMensaje() + "'");

        FlujoMensajeResponseDTO dto = toDTO(flujo);
        dto.setAdvertencias(advertencias);
        return dto;
    }

    @Override
    @Transactional
    public FlujoMensajeResponseDTO editarFlujo(Long procesoId, Long flujoId,
                                                FlujoMensajeRequestDTO request,
                                                Long empresaId, RolUsuario rol) {
        verificarPermiso(rol);
        Proceso proceso = verificarProceso(procesoId, empresaId);
        validarProcesoActivo(proceso);
        FlujoMensaje flujo = flujoRepository.findByIdAndProcesoIdAndActivoTrue(flujoId, procesoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Flujo de mensaje no encontrado"));

        List<String> advertencias = aplicarDatos(flujo, request, procesoId, flujoId);
        flujo = flujoRepository.save(flujo);

        registrarHistorial(proceso,
                "Se editó un flujo de mensaje desde '" + flujo.getOrigen().getNombreMensaje() + "'");

        FlujoMensajeResponseDTO dto = toDTO(flujo);
        dto.setAdvertencias(advertencias);
        return dto;
    }

    @Override
    @Transactional
    public EliminacionFlujoMensajeResponseDTO eliminarFlujo(Long procesoId, Long flujoId,
                                                             Long empresaId, RolUsuario rol) {
        verificarPermiso(rol);
        Proceso proceso = verificarProceso(procesoId, empresaId);
        validarProcesoActivo(proceso);
        FlujoMensaje flujo = flujoRepository.findByIdAndProcesoIdAndActivoTrue(flujoId, procesoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Flujo de mensaje no encontrado"));

        flujo.setActivo(false);
        flujoRepository.save(flujo);

        registrarHistorial(proceso,
                "Se eliminó un flujo de mensaje desde '" + flujo.getOrigen().getNombreMensaje() + "'");

        return new EliminacionFlujoMensajeResponseDTO(
                "Flujo de mensaje eliminado correctamente", new ArrayList<>());
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

    private void validarProcesoActivo(Proceso proceso) {
        if (!Boolean.TRUE.equals(proceso.getActivo())) {
            throw new OperacionInvalidaException(
                    "No se pueden modificar flujos de mensaje de un proceso eliminado");
        }
    }

    private List<String> aplicarDatos(FlujoMensaje flujo, FlujoMensajeRequestDTO request,
                                      Long procesoId, Long flujoIdActual) {
        if (request.getDestinoId() != null && request.getPoolDestinoId() != null) {
            throw new OperacionInvalidaException(
                    "Debe indicar un único destino: Message Catch o pool externo");
        }
        if (request.getDestinoId() == null && request.getPoolDestinoId() == null) {
            throw new OperacionInvalidaException(
                    "Debe indicar un Message Catch destino o un pool externo destino");
        }

        EventoMensaje origen = eventoRepository
                .findByIdAndProcesoIdAndActivoTrue(request.getOrigenId(), procesoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Evento origen no encontrado"));

        if (origen.getTipo() != TipoEventoMensaje.THROW) {
            throw new OperacionInvalidaException(
                    "El flujo de mensaje debe partir de un Message Throw");
        }

        EventoMensaje destino = null;
        Pool poolDestino = null;
        List<String> advertencias = new ArrayList<>();

        if (request.getDestinoId() != null) {
            destino = eventoRepository
                    .findByIdAndProcesoIdAndActivoTrue(request.getDestinoId(), procesoId)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Evento destino no encontrado"));
            if (destino.getTipo() == TipoEventoMensaje.THROW) {
                throw new OperacionInvalidaException(
                        "El destino de un flujo de mensaje debe ser un Message Catch");
            }
            if (destino.getPool().getId().equals(origen.getPool().getId())) {
                throw new OperacionInvalidaException(
                        "Un flujo de mensaje solo es válido si cruza de un pool a otro");
            }
            validarDuplicadoInterno(procesoId, origen.getId(), destino.getId(), flujoIdActual);
            if (!origen.getNombreMensaje().equals(destino.getNombreMensaje())) {
                advertencias.add("El nombre del mensaje del origen ('"
                        + origen.getNombreMensaje()
                        + "') no coincide con el del destino ('"
                        + destino.getNombreMensaje() + "')");
            }
            if (origen.getClaveCorrelacion() != null
                    && !origen.getClaveCorrelacion().isBlank()
                    && destino.getClaveCorrelacion() != null
                    && !destino.getClaveCorrelacion().isBlank()
                    && !origen.getClaveCorrelacion().equals(destino.getClaveCorrelacion())) {
                advertencias.add("La clave de correlación del Message Throw no coincide con la del Message Catch destino");
            }
        } else {
            poolDestino = poolRepository.findByIdAndProcesoIdAndActivoTrue(request.getPoolDestinoId(), procesoId)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Pool destino no encontrado"));
            if (poolDestino.getTipo() != TipoPool.EXTERNO) {
                throw new OperacionInvalidaException(
                        "El pool destino externo debe tener tipo EXTERNO");
            }
            if (poolDestino.getId().equals(origen.getPool().getId())) {
                throw new OperacionInvalidaException(
                        "El pool destino del mensaje debe ser distinto al pool origen");
            }
            if (origen.getTipoDestinoExterno() == null) {
                throw new OperacionInvalidaException(
                        "Debe indicar el tipo de destino externo en el Message Throw");
            }
            if (origen.getComportamientoFallo() == null) {
                throw new OperacionInvalidaException(
                        "Debe indicar el comportamiento ante fallo en el Message Throw");
            }
            validarDuplicadoExterno(procesoId, origen.getId(), poolDestino.getId(), flujoIdActual);
        }

        flujo.setOrigen(origen);
        flujo.setDestino(destino);
        flujo.setPoolDestino(poolDestino);
        flujo.setEtiqueta(request.getEtiqueta());
        return advertencias;
    }

    private void validarDuplicadoInterno(Long procesoId, Long origenId, Long destinoId, Long flujoIdActual) {
        boolean duplicado = flujoIdActual == null
                ? flujoRepository.existsByProcesoIdAndOrigenIdAndDestinoIdAndActivoTrue(procesoId, origenId, destinoId)
                : flujoRepository.existsByProcesoIdAndOrigenIdAndDestinoIdAndActivoTrueAndIdNot(
                        procesoId, origenId, destinoId, flujoIdActual);
        if (duplicado) {
            throw new OperacionInvalidaException("Ya existe un flujo de mensaje activo entre esos eventos");
        }
    }

    private void validarDuplicadoExterno(Long procesoId, Long origenId, Long poolDestinoId, Long flujoIdActual) {
        boolean duplicado = flujoIdActual == null
                ? flujoRepository.existsByProcesoIdAndOrigenIdAndPoolDestinoIdAndActivoTrue(
                        procesoId, origenId, poolDestinoId)
                : flujoRepository.existsByProcesoIdAndOrigenIdAndPoolDestinoIdAndActivoTrueAndIdNot(
                        procesoId, origenId, poolDestinoId, flujoIdActual);
        if (duplicado) {
            throw new OperacionInvalidaException("Ya existe un flujo de mensaje activo hacia ese pool externo");
        }
    }

    private void registrarHistorial(Proceso proceso, String descripcion) {
        historialRepository.save(new HistorialProceso(
                proceso, descripcion, LocalDateTime.now()));
    }

    private FlujoMensajeResponseDTO toDTO(FlujoMensaje f) {
        FlujoMensajeResponseDTO dto = new FlujoMensajeResponseDTO();
        dto.setId(f.getId());
        dto.setProcesoId(f.getProceso().getId());
        dto.setOrigenId(f.getOrigen().getId());
        if (f.getDestino() != null) dto.setDestinoId(f.getDestino().getId());
        if (f.getPoolDestino() != null) dto.setPoolDestinoId(f.getPoolDestino().getId());
        dto.setEtiqueta(f.getEtiqueta());
        return dto;
    }
}
