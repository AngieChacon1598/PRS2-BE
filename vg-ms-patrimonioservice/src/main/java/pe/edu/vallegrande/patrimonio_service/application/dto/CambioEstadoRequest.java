package pe.edu.vallegrande.patrimonio_service.application.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class CambioEstadoRequest {
    private String nuevoEstado;
    private String observaciones;
    private UUID modificadoPor;
}