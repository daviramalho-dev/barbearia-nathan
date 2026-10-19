package br.com.barbearia_nathan.exception;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponse(
        int status,
        String message,
        LocalDateTime timestamp,
        Map<String, String> errors
) {

    public ErrorResponse(int status, String message, LocalDateTime timestamp) {
        this(status, message, timestamp, Map.of());
    }

    public ErrorResponse {
        errors = Map.copyOf(errors);
    }
}

