package com.agonzalorena.msvc.simulator.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"timestamp", "status", "message"})
public record ErrorResponse(String timestamp, int status, String message) {
    public ErrorResponse(int status, String message) {
        //String para no tener problemas con TokenAuthFilter
        this(java.time.LocalDateTime.now().toString(), status, message);
    }
    //posible opcional agregar data y poner 2 constructores
}
