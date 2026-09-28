package com.example.backend.service;

import com.example.backend.dto.EdicionGatewayResponseDTO;
import com.example.backend.dto.EliminacionGatewayResponseDTO;
import com.example.backend.dto.GatewayRequestDTO;
import com.example.backend.dto.GatewayResponseDTO;
import com.example.backend.entity.Arco;
import com.example.backend.entity.Gateway;
import com.example.backend.entity.HistorialProceso;
import com.example.backend.entity.Pool;
import com.example.backend.entity.Proceso;
import com.example.backend.entity.RolUsuario;
import com.example.backend.entity.TipoGateway;
import com.example.backend.entity.TipoNodo;
import com.example.backend.entity.TipoPool;
import com.example.backend.exception.OperacionInvalidaException;
import com.example.backend.exception.PermisoDenegadoException;
import com.example.backend.exception.RecursoNoEncontradoException;
import com.example.backend.repository.ArcoRepository;
import com.example.backend.repository.GatewayRepository;
import com.example.backend.repository.HistorialProcesoRepository;
import com.example.backend.repository.PoolRepository;
import com.example.backend.repository.ProcesoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class GatewayServiceImpl implements GatewayService {

    private final GatewayRepository gatewayRepository;
    private final ProcesoRepository procesoRepository;
    private final HistorialProcesoRepository historialProcesoRepository;
    private final ArcoRepository arcoRepository;
    private final PoolRepository poolRepository;

    public GatewayServiceImpl(
            GatewayRepository gatewayRepository,
            ProcesoRepository procesoRepository,
            HistorialProcesoRepository historialProcesoRepository,
            ArcoRepository arcoRepository,
            PoolRepository poolRepository) {

        this.gatewayRepository = gatewayRepository;
        this.procesoRepository = procesoRepository;
        this.historialProcesoRepository = historialProcesoRepository;
        this.arcoRepository = arcoRepository;
        this.poolRepository = poolRepository;
    }

    @Override
    @Transactional
    public GatewayResponseDTO crearGateway(
            Long procesoId,
            GatewayRequestDTO request,
            Long empresaId,
            RolUsuario rol) {

        validarPermisoEdicion(rol);

        Proceso proceso = obtenerProceso(
                procesoId,
                empresaId
        );

        validarProcesoActivo(proceso);
        validarRequest(request);
        Pool pool = obtenerPoolValido(request.getPoolId(), procesoId);

        Gateway gateway = new Gateway();

        gateway.setTipo(request.getTipo());
        gateway.setProceso(proceso);
        gateway.setPool(pool);
        gateway.setPosicionX(request.getPosicionX());
        gateway.setPosicionY(request.getPosicionY());
        gateway.setActivo(true);

        gateway = gatewayRepository.save(gateway);

        historialProcesoRepository.save(
                new HistorialProceso(
                        proceso,
                        "Se creó el gateway #"
                                + gateway.getId()
                                + " de tipo "
                                + gateway.getTipo(),
                        LocalDateTime.now()
                )
        );

        return convertirDTO(gateway);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GatewayResponseDTO> listarPorProceso(
            Long procesoId,
            Long empresaId) {

        obtenerProceso(
                procesoId,
                empresaId
        );

        return gatewayRepository
                .findByProcesoIdAndActivoTrue(procesoId)
                .stream()
                .map(this::convertirDTO)
                .toList();
    }

    @Override
    @Transactional
    public EdicionGatewayResponseDTO editarGateway(
            Long procesoId,
            Long gatewayId,
            GatewayRequestDTO request,
            Long empresaId,
            RolUsuario rol) {

        validarPermisoEdicion(rol);

        Proceso proceso = obtenerProceso(
                procesoId,
                empresaId
        );

        validarProcesoActivo(proceso);
        validarRequest(request);
        Pool pool = obtenerPoolValido(request.getPoolId(), procesoId);

        Gateway gateway = gatewayRepository
                .findByIdAndProcesoIdAndActivoTrue(
                        gatewayId,
                        procesoId
                )
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "El gateway no existe, está eliminado o no pertenece al proceso"
                        )
                );

        TipoGateway tipoAnterior = gateway.getTipo();

        List<Arco> arcosSalientes = arcoRepository
                .findByProcesoId(procesoId)
                .stream()
                .filter(arco ->
                        arco.getTipoOrigen() == TipoNodo.GATEWAY
                                && gatewayId.equals(arco.getOrigenId())
                )
                .toList();

        List<String> advertencias =
                new ArrayList<>();

        if (request.getTipo() == TipoGateway.PARALELO) {

            for (Arco arco : arcosSalientes) {
                arco.setCondicion(null);
            }

            if (!arcosSalientes.isEmpty()) {
                arcoRepository.saveAll(arcosSalientes);

                advertencias.add(
                        "Se eliminaron las condiciones de los arcos salientes porque el gateway ahora es PARALELO"
                );
            }
        }

        if (request.getTipo() == TipoGateway.EXCLUSIVO) {

            long sinCondicion = arcosSalientes
                    .stream()
                    .filter(arco ->
                            arco.getCondicion() == null
                                    || arco.getCondicion().isBlank()
                    )
                    .count();

            if (sinCondicion > 0) {
                advertencias.add(
                        "El gateway EXCLUSIVO tiene "
                                + sinCondicion
                                + " arco(s) saliente(s) sin condición"
                );
            }

            if (arcosSalientes.size() < 2) {
                advertencias.add(
                        "Un gateway EXCLUSIVO de divergencia debe tener al menos dos arcos salientes"
                );
            }

            advertencias.add(
                    "Verifique que las condiciones de los arcos salientes sean mutuamente excluyentes"
            );
        }

        if (request.getTipo() == TipoGateway.INCLUSIVO) {

            long sinCondicion = arcosSalientes
                    .stream()
                    .filter(arco ->
                            arco.getCondicion() == null
                                    || arco.getCondicion().isBlank()
                    )
                    .count();

            if (sinCondicion > 0) {
                advertencias.add(
                        "El gateway INCLUSIVO tiene "
                                + sinCondicion
                                + " arco(s) saliente(s) sin condición"
                );
            }

            if (arcosSalientes.size() < 2) {
                advertencias.add(
                        "Un gateway INCLUSIVO de divergencia debe tener al menos dos arcos salientes"
                );
            }
        }

        gateway.setTipo(request.getTipo());
        gateway.setPool(pool);
        gateway.setPosicionX(request.getPosicionX());
        gateway.setPosicionY(request.getPosicionY());

        gateway = gatewayRepository.save(gateway);

        historialProcesoRepository.save(
                new HistorialProceso(
                        proceso,
                        "Se editó el gateway #"
                                + gateway.getId()
                                + " de "
                                + tipoAnterior
                                + " a "
                                + gateway.getTipo(),
                        LocalDateTime.now()
                )
        );

        return new EdicionGatewayResponseDTO(
                convertirDTO(gateway),
                advertencias
        );
    }

    @Override
    @Transactional
    public EliminacionGatewayResponseDTO eliminarGateway(
            Long procesoId,
            Long gatewayId,
            Long empresaId,
            RolUsuario rol,
            boolean confirmar) {

        if (rol != RolUsuario.ADMINISTRADOR) {
            throw new PermisoDenegadoException(
                    "Solo un administrador puede eliminar gateways"
            );
        }

        Proceso proceso = obtenerProceso(
                procesoId,
                empresaId
        );

        validarProcesoActivo(proceso);

        Gateway gateway = gatewayRepository
                .findByIdAndProcesoIdAndActivoTrue(
                        gatewayId,
                        procesoId
                )
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "El gateway no existe, está eliminado o no pertenece al proceso"
                        )
                );

        List<Arco> arcosConectados = arcoRepository
                .findByProcesoId(procesoId)
                .stream()
                .filter(arco ->
                        (arco.getTipoOrigen() == TipoNodo.GATEWAY
                                && gatewayId.equals(arco.getOrigenId()))
                                ||
                        (arco.getTipoDestino() == TipoNodo.GATEWAY
                                && gatewayId.equals(arco.getDestinoId()))
                )
                .toList();

        long arcosSalientes = arcosConectados
                .stream()
                .filter(arco ->
                        arco.getTipoOrigen() == TipoNodo.GATEWAY
                                && gatewayId.equals(arco.getOrigenId())
                )
                .count();

        if (!confirmar) {

            String mensaje =
                    "Debe confirmar la eliminación del gateway";

            if (!arcosConectados.isEmpty()) {
                mensaje += ". Se eliminarán "
                        + arcosConectados.size()
                        + " arco(s) conectado(s)";
            }

            if (arcosSalientes >= 2) {
                mensaje +=
                        ". La ramificación quedará sin punto de decisión y el flujo puede perder coherencia";
            }

            throw new OperacionInvalidaException(
                    mensaje
            );
        }

        List<String> advertencias =
                new ArrayList<>();

        if (arcosSalientes >= 2) {
            advertencias.add(
                    "La ramificación quedó sin su punto de decisión y el flujo puede perder coherencia"
            );
        }

        if (!arcosConectados.isEmpty()) {
            advertencias.add(
                    "Se eliminaron "
                            + arcosConectados.size()
                            + " arco(s) conectado(s) al gateway"
            );
        }

        int cantidadArcos =
                arcosConectados.size();

        arcoRepository.deleteAll(arcosConectados);

        gateway.setActivo(false);
        gatewayRepository.save(gateway);

        historialProcesoRepository.save(
                new HistorialProceso(
                        proceso,
                        "Se eliminó lógicamente el gateway #"
                                + gateway.getId()
                                + " junto con "
                                + cantidadArcos
                                + " arco(s) conectado(s)",
                        LocalDateTime.now()
                )
        );

        return new EliminacionGatewayResponseDTO(
                gateway.getId(),
                cantidadArcos,
                advertencias
        );
    }

    private Proceso obtenerProceso(
            Long procesoId,
            Long empresaId) {

        return procesoRepository
                .findByIdAndEmpresaId(
                        procesoId,
                        empresaId
                )
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "El proceso no existe en la empresa indicada"
                        )
                );
    }

    private void validarProcesoActivo(
            Proceso proceso) {

        if (!Boolean.TRUE.equals(proceso.getActivo())) {
            throw new OperacionInvalidaException(
                    "No se pueden modificar gateways de un proceso eliminado"
            );
        }
    }

    private void validarPermisoEdicion(
            RolUsuario rol) {

        if (rol != RolUsuario.ADMINISTRADOR
                && rol != RolUsuario.EDITOR) {

            throw new PermisoDenegadoException(
                    "El usuario no tiene permisos para crear o editar gateways"
            );
        }
    }

    private void validarRequest(
            GatewayRequestDTO request) {

        if (request == null) {
            throw new OperacionInvalidaException(
                    "Los datos del gateway son obligatorios"
            );
        }

        if (request.getTipo() == null) {
            throw new OperacionInvalidaException(
                    "El tipo de gateway es obligatorio"
            );
        }

        if (request.getPoolId() == null) {
            throw new OperacionInvalidaException(
                    "Debe indicar el pool del gateway"
            );
        }
    }

    private Pool obtenerPoolValido(Long poolId, Long procesoId) {
        Pool pool = poolRepository.findByIdAndProcesoId(poolId, procesoId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "El pool indicado no existe en este proceso"
                        )
                );

        if (!Boolean.TRUE.equals(pool.getActivo())) {
            throw new OperacionInvalidaException(
                    "El pool indicado está eliminado"
            );
        }

        if (pool.getTipo() == TipoPool.EXTERNO) {
            throw new OperacionInvalidaException(
                    "Un pool externo no puede contener gateways"
            );
        }

        return pool;
    }

    private GatewayResponseDTO convertirDTO(
            Gateway gateway) {

        GatewayResponseDTO dto =
                new GatewayResponseDTO();

        dto.setId(gateway.getId());
        dto.setProcesoId(
                gateway.getProceso().getId()
        );
        if (gateway.getPool() != null) {
            dto.setPoolId(gateway.getPool().getId());
        }
        dto.setTipo(gateway.getTipo());
        dto.setPosicionX(
                gateway.getPosicionX()
        );
        dto.setPosicionY(
                gateway.getPosicionY()
        );

        return dto;
    }
}
