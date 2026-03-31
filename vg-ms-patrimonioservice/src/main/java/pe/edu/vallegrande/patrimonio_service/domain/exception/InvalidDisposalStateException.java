package pe.edu.vallegrande.patrimonio_service.domain.exception;

public class InvalidDisposalStateException extends RuntimeException {
    public InvalidDisposalStateException(String message) {
        super(message);
    }
}
