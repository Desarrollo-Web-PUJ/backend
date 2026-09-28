package com.example.backend.service;

import com.example.backend.dto.*;
import com.example.backend.entity.*;
import com.example.backend.exception.OperacionInvalidaException;
import com.example.backend.exception.PermisoDenegadoException;
import com.example.backend.exception.RecursoDuplicadoException;
import com.example.backend.exception.RecursoNoEncontradoException;
import com.example.backend.repository.ActividadRepository;
import com.example.backend.repository.ArcoRepository;
import com.example.backend.repository.EmpresaRepository;
import com.example.backend.repository.EventoMensajeRepository;
import com.example.backend.repository.FlujoMensajeRepository;
import com.example.backend.repository.GatewayRepository;
import com.example.backend.repository.HistorialProcesoRepository;
import com.example.backend.repository.LaneRepository;
import com.example.backend.repository.PoolRepository;
import com.example.backend.repository.ProcesoCompartidoRepository;
import com.example.backend.repository.ProcesoRepository;
import com.example.backend.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProcesoServiceImpl implements ProcesoService {

    private final ProcesoRepository procesoRepository;
    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;
    private final HistorialProcesoRepository historialProcesoRepository;
    private final PoolRepository poolRepository;
    private final LaneRepository laneRepository;
    private final ActividadRepository actividadRepository;
    private final GatewayRepository gatewayRepository;
    private final ArcoRepository arcoRepository;
    private final EventoMensajeRepository eventoMensajeRepository;
    private final FlujoMensajeRepository flujoMensajeRepository;
    private final ProcesoCompartidoRepository procesoCompartidoRepository;
    private final JsonMapper objectMapper;

    public ProcesoServiceImpl(
            ProcesoRepository procesoRepository,
            EmpresaRepository empresaRepository,
            UsuarioRepository usuarioRepository,
            HistorialProcesoRepository historialProcesoRepository,
            PoolRepository poolRepository,
            LaneRepository laneRepository,
            ActividadRepository actividadRepository,
            GatewayRepository gatewayRepository,
            ArcoRepository arcoRepository,
            EventoMensajeRepository eventoMensajeRepository,
            FlujoMensajeRepository flujoMensajeRepository,
            ProcesoCompartidoRepository procesoCompartidoRepository,
            JsonMapper objectMapper) {

        this.procesoRepository = procesoRepository;
        this.empresaRepository = empresaRepository;
        this.usuarioRepository = usuarioRepository;
        this.historialProcesoRepository = historialProcesoRepository;
        this.poolRepository = poolRepository;
        this.laneRepository = laneRepository;
        this.actividadRepository = actividadRepository;
        this.gatewayRepository = gatewayRepository;
        this.arcoRepository = arcoRepository;
        this.eventoMensajeRepository = eventoMensajeRepository;
        this.flujoMensajeRepository = flujoMensajeRepository;
        this.procesoCompartidoRepository = procesoCompartidoRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public ProcesoResponseDTO crearProceso(
            Long empresaId,
            Long usuarioId,
            RolUsuario rol,
            ProcesoCrearRequestDTO request) {

        if (rol != RolUsuario.ADMINISTRADOR
                && rol != RolUsuario.EDITOR) {

            throw new PermisoDenegadoException(
                    "No tienes permisos para crear procesos"
            );
        }

        if (request.getNombre() == null
                || request.getNombre().isBlank()) {

            throw new OperacionInvalidaException(
                    "El nombre del proceso es obligatorio"
            );
        }

        if (request.getCategoria() == null
                || request.getCategoria().isBlank()) {

            throw new OperacionInvalidaException(
                    "La categoria es obligatoria"
            );
        }

        if (procesoRepository.existsByNombreAndEmpresaId(
                request.getNombre(),
                empresaId)) {

            throw new RecursoDuplicadoException(
                    "Ya existe un proceso con ese nombre en la empresa"
            );
        }

        Empresa empresa = empresaRepository
                .findById(empresaId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "La empresa no existe"
                        )
                );

        Usuario usuario = usuarioRepository
                .findById(usuarioId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "El usuario no existe"
                        )
                );

        Proceso proceso = new Proceso();

        proceso.setNombre(request.getNombre());
        proceso.setDescripcion(request.getDescripcion());
        proceso.setCategoria(request.getCategoria());
        proceso.setEstado(EstadoProceso.BORRADOR);
        proceso.setActivo(true);
        proceso.setEmpresa(empresa);

        proceso = procesoRepository.save(proceso);

        Pool poolPropio = new Pool();
        poolPropio.setNombre(empresa.getNombre());
        poolPropio.setTipo(TipoPool.PROPIO);
        poolPropio.setProceso(proceso);
        poolPropio.setActivo(true);
        poolRepository.save(poolPropio);

        registrarHistorial(
                proceso,
                "Se creo el proceso '"
                        + proceso.getNombre()
                        + "' (usuario: "
                        + usuario.getNombre()
                        + ")"
        );

        return toResponseDTO(proceso);
    }

    @Override
    @Transactional
    public ProcesoResponseDTO editarProceso(
            Long procesoId,
            Long empresaId,
            Long usuarioId,
            RolUsuario rol,
            ProcesoEditarRequestDTO request) {

        if (rol != RolUsuario.ADMINISTRADOR
                && rol != RolUsuario.EDITOR) {

            throw new PermisoDenegadoException(
                    "No tienes permisos para editar procesos"
            );
        }

        Proceso proceso = procesoRepository
                .findByIdAndEmpresaId(
                        procesoId,
                        empresaId
                )
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "El proceso no existe"
                        )
                );

        Usuario usuario = usuarioRepository
                .findById(usuarioId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "El usuario no existe"
                        )
                );

        if (request.getNombre() == null
                || request.getNombre().isBlank()) {

            throw new OperacionInvalidaException(
                    "El nombre del proceso es obligatorio"
            );
        }

        if (request.getCategoria() == null
                || request.getCategoria().isBlank()) {

            throw new OperacionInvalidaException(
                    "La categoria es obligatoria"
            );
        }

        if (request.getEstado() == null) {
            throw new OperacionInvalidaException(
                    "El estado es obligatorio"
            );
        }

        if (!proceso.getNombre()
                .equalsIgnoreCase(request.getNombre())
                && procesoRepository
                .existsByNombreAndEmpresaIdAndIdNot(
                        request.getNombre(),
                        empresaId,
                        procesoId
                )) {

            throw new RecursoDuplicadoException(
                    "Ya existe un proceso con ese nombre en la empresa"
            );
        }

        proceso.setNombre(request.getNombre());
        proceso.setDescripcion(request.getDescripcion());
        proceso.setCategoria(request.getCategoria());
        proceso.setEstado(request.getEstado());

        proceso = procesoRepository.save(proceso);

        registrarHistorial(
                proceso,
                "Se edito el proceso '"
                        + proceso.getNombre()
                        + "' (usuario: "
                        + usuario.getNombre()
                        + ")"
        );

        return toResponseDTO(proceso);
    }

    @Override
    @Transactional
    public void eliminarProceso(
            Long procesoId,
            Long empresaId,
            Long usuarioId,
            RolUsuario rol) {

        if (rol != RolUsuario.ADMINISTRADOR) {
            throw new PermisoDenegadoException(
                    "Solo un administrador puede eliminar procesos"
            );
        }

        Proceso proceso = procesoRepository
                .findByIdAndEmpresaId(
                        procesoId,
                        empresaId
                )
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "El proceso no existe"
                        )
                );

        Usuario usuario = usuarioRepository
                .findById(usuarioId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "El usuario no existe"
                        )
                );

        proceso.setActivo(false);
        procesoRepository.save(proceso);

        registrarHistorial(
                proceso,
                "Se elimino (logicamente) el proceso '"
                        + proceso.getNombre()
                        + "' (usuario: "
                        + usuario.getNombre()
                        + ")"
        );
    }

    @Override
    public Page<ProcesoListItemDTO> listarProcesos(
            Long empresaId,
            String nombre,
            EstadoProceso estado,
            String categoria,
            boolean incluirInactivos,
            Pageable pageable) {

        String nombreFiltro =
                nombre == null
                        ? ""
                        : nombre.trim();

        return procesoRepository
                .buscar(
                        empresaId,
                        incluirInactivos,
                        nombreFiltro,
                        estado,
                        categoria,
                        pageable
                )
                .map(p ->
                        new ProcesoListItemDTO(
                                p.getId(),
                                p.getNombre(),
                                p.getCategoria(),
                                p.getEstado(),
                                p.getActivo()
                        )
                );
    }

    @Override
    @Transactional(readOnly = true)
    public ProcesoDetalleDTO obtenerDetalle(
            Long procesoId,
            Long empresaId) {

        Proceso proceso = obtenerProcesoPropioOCompartido(
                procesoId,
                empresaId
        );

        List<PoolResponseDTO> pools = poolRepository
                .findByProcesoIdAndActivoTrue(procesoId)
                .stream()
                .map(this::toPoolDTO)
                .toList();

        List<LaneDTO> lanes = laneRepository
                .findByProcesoIdAndActivoTrueOrderByOrdenAsc(procesoId)
                .stream()
                .map(this::toLaneDTO)
                .toList();

        List<ActividadDTO> actividades = actividadRepository
                .findByProcesoIdAndActivoTrue(procesoId)
                .stream()
                .map(this::toActividadDTO)
                .toList();

        List<GatewayResponseDTO> gateways = gatewayRepository
                .findByProcesoIdAndActivoTrue(procesoId)
                .stream()
                .map(this::toGatewayDTO)
                .toList();

        List<ArcoResponseDTO> arcos = arcoRepository
                .findByProcesoIdAndActivoTrue(procesoId)
                .stream()
                .map(this::toArcoDTO)
                .toList();

        List<EventoMensajeResponseDTO> eventosMensaje = eventoMensajeRepository
                .findByProcesoIdAndActivoTrue(procesoId)
                .stream()
                .map(this::toEventoMensajeDTO)
                .toList();

        List<FlujoMensajeResponseDTO> flujosMensaje = flujoMensajeRepository
                .findByProcesoIdAndActivoTrue(procesoId)
                .stream()
                .map(this::toFlujoMensajeDTO)
                .toList();

        return new ProcesoDetalleDTO(
                proceso.getId(),
                proceso.getNombre(),
                proceso.getDescripcion(),
                proceso.getCategoria(),
                proceso.getEstado(),
                proceso.getActivo(),
                pools,
                lanes,
                actividades,
                gateways,
                arcos,
                eventosMensaje,
                flujosMensaje
        );
    }

    @Override
    public List<HistorialProcesoDTO> obtenerHistorial(
            Long procesoId,
            Long empresaId) {

        procesoRepository
                .findByIdAndEmpresaId(
                        procesoId,
                        empresaId
                )
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "El proceso no existe"
                        )
                );

        return historialProcesoRepository
                .findByProcesoIdOrderByFechaDesc(
                        procesoId
                )
                .stream()
                .map(h ->
                        new HistorialProcesoDTO(
                                h.getId(),
                                h.getDescripcion(),
                                h.getFecha()
                        )
                )
                .collect(Collectors.toList());
    }

    private Proceso obtenerProcesoPropioOCompartido(
            Long procesoId,
            Long empresaId) {

        return procesoRepository
                .findByIdAndEmpresaId(procesoId, empresaId)
                .orElseGet(() -> {
                    if (!procesoCompartidoRepository
                            .existsByProcesoIdAndEmpresaDestinoIdAndActivoTrue(
                                    procesoId,
                                    empresaId
                            )) {

                        throw new RecursoNoEncontradoException(
                                "El proceso no existe en la empresa indicada"
                        );
                    }

                    Proceso compartido = procesoRepository
                            .findById(procesoId)
                            .orElseThrow(() ->
                                    new RecursoNoEncontradoException(
                                            "El proceso compartido no existe"
                                    )
                            );

                    if (!Boolean.TRUE.equals(compartido.getActivo())) {
                        throw new RecursoNoEncontradoException(
                                "El proceso compartido no está activo"
                        );
                    }

                    return compartido;
                });
    }

    private PoolResponseDTO toPoolDTO(Pool pool) {

        PoolResponseDTO dto = new PoolResponseDTO();
        dto.setId(pool.getId());
        dto.setNombre(pool.getNombre());
        dto.setTipo(pool.getTipo());
        dto.setProcesoId(pool.getProceso().getId());
        dto.setCantidadLanes(
                laneRepository
                        .findByPoolIdAndActivoTrueOrderByOrdenAsc(pool.getId())
                        .size()
        );
        return dto;
    }

    private LaneDTO toLaneDTO(Lane lane) {

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

    private ActividadDTO toActividadDTO(Actividad actividad) {

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

    private GatewayResponseDTO toGatewayDTO(Gateway gateway) {

        GatewayResponseDTO dto = new GatewayResponseDTO();
        dto.setId(gateway.getId());
        dto.setProcesoId(gateway.getProceso().getId());
        if (gateway.getPool() != null) {
            dto.setPoolId(gateway.getPool().getId());
        }
        dto.setTipo(gateway.getTipo());
        dto.setPosicionX(gateway.getPosicionX());
        dto.setPosicionY(gateway.getPosicionY());
        return dto;
    }

    private ArcoResponseDTO toArcoDTO(Arco arco) {

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

    private EventoMensajeResponseDTO toEventoMensajeDTO(EventoMensaje evento) {

        EventoMensajeResponseDTO dto = new EventoMensajeResponseDTO();
        dto.setId(evento.getId());
        dto.setProcesoId(evento.getProceso().getId());
        dto.setPoolId(evento.getPool().getId());
        dto.setPoolNombre(evento.getPool().getNombre());
        if (evento.getLane() != null) {
            dto.setLaneId(evento.getLane().getId());
            dto.setLaneNombre(evento.getLane().getNombre());
        }
        dto.setTipo(evento.getTipo());
        dto.setNombreMensaje(evento.getNombreMensaje());
        dto.setClaveCorrelacion(evento.getClaveCorrelacion());
        dto.setCorrelacionDefinida(evento.getCorrelacionDefinida());
        dto.setComportamientoSinCorrelacion(evento.getComportamientoSinCorrelacion());
        dto.setOrigenExterno(evento.getOrigenExterno());
        dto.setTipoDestinoExterno(evento.getTipoDestinoExterno());
        dto.setComportamientoFallo(evento.getComportamientoFallo());
        dto.setPosicionX(evento.getPosicionX());
        dto.setPosicionY(evento.getPosicionY());

        try {
            if (evento.getCamposMensaje() != null) {
                dto.setCampos(objectMapper.readValue(
                        evento.getCamposMensaje(),
                        new TypeReference<List<CampoMensajeDTO>>() {}
                ));
            }
            if (evento.getActividadesConsumidoras() != null) {
                dto.setActividadesConsumidoras(objectMapper.readValue(
                        evento.getActividadesConsumidoras(),
                        new TypeReference<List<Long>>() {}
                ));
            }
        } catch (Exception ignored) {
        }

        return dto;
    }

    private FlujoMensajeResponseDTO toFlujoMensajeDTO(FlujoMensaje flujo) {

        FlujoMensajeResponseDTO dto = new FlujoMensajeResponseDTO();
        dto.setId(flujo.getId());
        dto.setProcesoId(flujo.getProceso().getId());
        dto.setOrigenId(flujo.getOrigen().getId());
        if (flujo.getDestino() != null) {
            dto.setDestinoId(flujo.getDestino().getId());
        }
        if (flujo.getPoolDestino() != null) {
            dto.setPoolDestinoId(flujo.getPoolDestino().getId());
        }
        dto.setEtiqueta(flujo.getEtiqueta());
        return dto;
    }

    private void registrarHistorial(
            Proceso proceso,
            String descripcion) {

        historialProcesoRepository.save(
                new HistorialProceso(
                        proceso,
                        descripcion,
                        LocalDateTime.now()
                )
        );
    }

    private ProcesoResponseDTO toResponseDTO(
            Proceso proceso) {

        return new ProcesoResponseDTO(
                proceso.getId(),
                proceso.getNombre(),
                proceso.getDescripcion(),
                proceso.getCategoria(),
                proceso.getEstado(),
                proceso.getActivo(),
                proceso.getEmpresa().getId()
        );
    }
}
