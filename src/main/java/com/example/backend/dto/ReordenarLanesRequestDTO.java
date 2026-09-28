package com.example.backend.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;

public class ReordenarLanesRequestDTO {

    @NotEmpty(message = "Debe indicar el orden de las lanes")
    private List<Long> laneIdsEnOrden;

    public List<Long> getLaneIdsEnOrden() { return laneIdsEnOrden; }
    public void setLaneIdsEnOrden(List<Long> laneIdsEnOrden) { this.laneIdsEnOrden = laneIdsEnOrden; }
}