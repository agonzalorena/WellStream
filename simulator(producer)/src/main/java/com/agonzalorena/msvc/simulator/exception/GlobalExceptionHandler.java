package com.agonzalorena.msvc.simulator.exception;

import com.agonzalorena.msvc.simulator.presentation.dto.response.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Default handler para cualquier excepción no manejada específicamente
    @ExceptionHandler(Exception.class)
    public ErrorResponse handleAllExceptions(Exception ex) {
        return new ErrorResponse(500, "Error interno del servidor: " + ex.getMessage());
    }

    // Url no encontrada (desactivar en application.yaml respuesta automatica de spring)
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFoundException(NoHandlerFoundException ex) {
        return ResponseEntity.status(404)
                .body(new ErrorResponse(404, "Not Found: " + ex.getRequestURL()));
    }

    // Maneja parametros faltantes en la solicitud,ejemplo: /matches?teamA=1 y no mandar teamA=
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(MissingServletRequestParameterException ex) {
        return ResponseEntity.status(400)
                .body(new ErrorResponse(400, "Bad Request: Missing parameter " + ex.getParameterName()));
    }

    // Maneja errores de tipo de parametro, ejemplo: /players?id=abc y se espera un Long
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.status(400)
                .body(new ErrorResponse(400, "Bad Request: Parameter type mismatch"));
    }

    // Maneja errores de validacion de argumentos
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.status(400)
                .body(new ErrorResponse(400, "Bad Request: " + ex.getMessage()));
    }
}
