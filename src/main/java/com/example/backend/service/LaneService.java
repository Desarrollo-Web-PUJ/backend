package com.example.backend.service;

import com.example.backend.dto.EliminacionLaneResponseDTO;
import com.example.backend.dto.LaneDTO;
import com.example.backend.dto.LaneRequestDTO;
import com.example.backend.dto.ReordenarLanesRequestDTO;
import com.example.backend.entity.RolUsuario;

import java.util.List;

public interface LaneService {

    List<LaneDTO> listarPorProceso(Long procesoId, Long empresaId);

    List<LaneDTO> listarPorPool(Long procesoId, Long poolId, Long empresaId);

    LaneDTO crearLaneEnPool(Long procesoId, Long poolId, LaneRequestDTO request, Long empresaId, RolUsuario rol);

    LaneDTO editarLane(Long procesoId, Long poolId, Long laneId, LaneRequestDTO request, Long empresaId, RolUsuario rol);

    List<LaneDTO> reordenarLanes(Long procesoId, Long poolId, ReordenarLanesRequestDTO request, Long empresaId, RolUsuario rol);

    EliminacionLaneResponseDTO eliminarLane(Long procesoId, Long poolId, Long laneId, Long empresaId, RolUsuario rol);
}
