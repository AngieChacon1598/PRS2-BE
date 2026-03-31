package pe.edu.vallegrande.configurationservice.infrastructure.adapters.input.rest.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import pe.edu.vallegrande.configurationservice.application.dto.ErrorResponse;
import pe.edu.vallegrande.configurationservice.domain.exception.DuplicateKeyException;
import pe.edu.vallegrande.configurationservice.domain.exception.MunicipalityNotFoundException;
import pe.edu.vallegrande.configurationservice.domain.exception.ValidationException;
import reactor.core.publisher.Mono;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ValidationException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleValidationException(ValidationException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Error de validación",
                ex.getMessage());
        return Mono.just(new ResponseEntity<>(error, HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleDuplicateKeyException(DuplicateKeyException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "Error de duplicidad",
                ex.getMessage());
        return Mono.just(new ResponseEntity<>(error, HttpStatus.CONFLICT));
    }

    @ExceptionHandler(MunicipalityNotFoundException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleNotFoundException(MunicipalityNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "No encontrado",
                ex.getMessage());
        return Mono.just(new ResponseEntity<>(error, HttpStatus.NOT_FOUND));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> handleGeneralException(Exception ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Error interno del servidor",
                ex.getMessage());
        return Mono.just(new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR));
    }
}
