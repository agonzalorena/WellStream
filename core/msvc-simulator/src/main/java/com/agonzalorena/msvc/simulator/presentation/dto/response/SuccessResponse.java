package com.agonzalorena.msvc.simulator.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.time.LocalDateTime;

@JsonPropertyOrder({"timestamp", "status", "data"})
public record SuccessResponse(
        LocalDateTime timestamp,
        int status,
        Object data
) {
    public SuccessResponse(int status, Object data) {
        this(LocalDateTime.now(), status, data);
    }
}
