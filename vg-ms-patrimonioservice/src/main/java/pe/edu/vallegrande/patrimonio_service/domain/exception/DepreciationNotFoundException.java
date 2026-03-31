package pe.edu.vallegrande.patrimonio_service.domain.exception;

public class DepreciationNotFoundException extends RuntimeException {
    public DepreciationNotFoundException(String message) {
        super(message);
    }
}
