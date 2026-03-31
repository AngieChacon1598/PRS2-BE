package pe.edu.vallegrande.ms_maintenanceService.infrastructure.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;

import io.r2dbc.spi.R2dbcException;
import lombok.extern.slf4j.Slf4j;
import pe.edu.vallegrande.ms_maintenanceService.application.dto.ErrorResponse;
import pe.edu.vallegrande.ms_maintenanceService.domain.exception.DuplicateMaintenanceCodeException;
import pe.edu.vallegrande.ms_maintenanceService.domain.exception.MaintenanceNotFoundException;
import pe.edu.vallegrande.ms_maintenanceService.domain.exception.MaintenanceValidationException;
import reactor.core.publisher.Mono;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleAccessDenied(
            AccessDeniedException ex, ServerWebExchange exchange) {
        log.warn("Acceso denegado en {}: {}", exchange.getRequest().getPath().value(), ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error("Acceso denegado")
                .message("No tiene permisos suficientes para realizar esta acción")
                .path(exchange.getRequest().getPath().value())
                .build();

        return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse));
    }

    @ExceptionHandler(MaintenanceNotFoundException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleMaintenanceNotFound(
            MaintenanceNotFoundException ex, ServerWebExchange exchange) {
        log.warn("Mantenimiento no encontrado: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Mantenimiento no encontrado")
                .message(ex.getMessage())
                .path(exchange.getRequest().getPath().value())
                .build();

        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse));
    }

    @ExceptionHandler(DuplicateMaintenanceCodeException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleDuplicateCode(
            DuplicateMaintenanceCodeException ex, ServerWebExchange exchange) {
        log.warn("Código de mantenimiento duplicado: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("Código de mantenimiento duplicado")
                .message(ex.getMessage())
                .path(exchange.getRequest().getPath().value())
                .build();

        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleValidationErrors(
            WebExchangeBindException ex, ServerWebExchange exchange) {
        log.warn("Errores de validación en la petición");

        Map<String, String> validationErrors = new HashMap<>();
        ex.getFieldErrors().forEach(error ->
                validationErrors.put(error.getField(), error.getDefaultMessage()));

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Error de validación")
                .message("Se encontraron errores de validación en los campos del formulario")
                .path(exchange.getRequest().getPath().value())
                .validationErrors(validationErrors)
                .build();

        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse));
    }

    @ExceptionHandler(MaintenanceValidationException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleBusinessValidationError(
            MaintenanceValidationException ex, ServerWebExchange exchange) {
        log.warn("Error de validación de negocio: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Error de validación de negocio")
                .message(ex.getMessage())
                .path(exchange.getRequest().getPath().value())
                .build();

        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse));
    }

    @ExceptionHandler({R2dbcException.class, DataAccessException.class})
    public Mono<ResponseEntity<ErrorResponse>> handleDatabaseError(
            Exception ex, ServerWebExchange exchange) {
        log.error("Error de conectividad con base de datos: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.SERVICE_UNAVAILABLE.value())
                .error("Servicio de datos no disponible")
                .message("No se pudo procesar la solicitud por un problema temporal de conexión con la base de datos")
                .path(exchange.getRequest().getPath().value())
                .build();

        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> handleGenericError(
            Exception ex, ServerWebExchange exchange) {
        log.error("Error interno del servidor: ", ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Error interno del servidor")
                .message("Ocurrió un error inesperado. Por favor, contacte al administrador")
                .path(exchange.getRequest().getPath().value())
                .build();

        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
    }
}

