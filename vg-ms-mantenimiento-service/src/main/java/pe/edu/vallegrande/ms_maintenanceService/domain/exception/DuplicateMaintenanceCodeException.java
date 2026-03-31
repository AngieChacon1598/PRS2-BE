package pe.edu.vallegrande.ms_maintenanceService.domain.exception;

public class DuplicateMaintenanceCodeException extends MaintenanceServiceException {

    public DuplicateMaintenanceCodeException(String message) {
        super(message);
    }
}

