package pe.edu.vallegrande.ms_inventory.infrastructure.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.resource.NoResourceFoundException;

import pe.edu.vallegrande.ms_inventory.domain.exception.ResourceNotFoundException;

import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

     private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

     // Error de negocio
     @ExceptionHandler(ResourceNotFoundException.class)
     public ResponseEntity<String> handleNotFound(ResourceNotFoundException ex) {

          String correlationId = UUID.randomUUID().toString();

          log.warn("Error ID: {} - Recurso no encontrado", correlationId);

          return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Recurso no encontrado. Código: " + correlationId);
     }

     // Error general
     @ExceptionHandler(Exception.class)
     public ResponseEntity<String> handleGeneric(Exception ex) {

          // Ignorar favicon.ico
          if (ex instanceof NoResourceFoundException) {
               return ResponseEntity
                         .status(HttpStatus.NOT_FOUND)
                         .body("Recurso no encontrado");
          }

          String correlationId = UUID.randomUUID().toString();

          log.error("Error ID: {} - Error interno", correlationId, ex);

          return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ocurrió un error interno. Código: " + correlationId);
     }
}