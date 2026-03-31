package pe.edu.vallegrande.configurationservice.domain.exception;

import java.util.UUID;

public class MunicipalityNotFoundException extends RuntimeException {
    public MunicipalityNotFoundException(UUID id) {
        super("No se encontró la municipalidad con ID: " + id);
    }
}
