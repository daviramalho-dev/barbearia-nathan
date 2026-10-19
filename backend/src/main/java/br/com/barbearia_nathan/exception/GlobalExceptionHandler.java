package br.com.barbearia_nathan.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new LinkedHashMap<>();

        for (FieldError error : exception.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(error.getField(), errorMessage(error));
        }

        for (ObjectError error : exception.getBindingResult().getGlobalErrors()) {
            errors.putIfAbsent("_global", errorMessage(error));
        }

        return errorResponse(HttpStatus.BAD_REQUEST, "Um ou mais campos são inválidos.", errors);
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            HandlerMethodValidationException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class
    })
    public ResponseEntity<ErrorResponse> handleInvalidRequest(Exception exception) {
        return errorResponse(HttpStatus.BAD_REQUEST, "A requisição contém dados inválidos.", Map.of());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NoResourceFoundException exception) {
        return errorResponse(HttpStatus.NOT_FOUND, "Recurso não encontrado.", Map.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception exception) {
        return errorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocorreu um erro interno no servidor.",
                Map.of()
        );
    }

    private ResponseEntity<ErrorResponse> errorResponse(
            HttpStatus status,
            String message,
            Map<String, String> errors
    ) {
        ErrorResponse response = new ErrorResponse(status.value(), message, LocalDateTime.now(), errors);

        return ResponseEntity
                .status(status)
                .body(response);
    }

    private String errorMessage(ObjectError error) {
        return error.getDefaultMessage() == null ? "Valor inválido." : error.getDefaultMessage();
    }
}
