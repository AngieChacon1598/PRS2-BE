package pe.edu.vallegrande.configurationservice.domain.exception;

public class DuplicateAssignmentException extends RuntimeException {
    public DuplicateAssignmentException(String message) {
        super(message);
    }
}
