package pe.edu.vallegrande.ms_maintenanceService.domain.exception;

public class MaintenanceServiceException extends RuntimeException {

    public MaintenanceServiceException(String message) {
        super(message);
    }

    public MaintenanceServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}

