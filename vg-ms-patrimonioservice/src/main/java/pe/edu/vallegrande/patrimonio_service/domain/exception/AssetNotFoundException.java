package pe.edu.vallegrande.patrimonio_service.domain.exception;

public class AssetNotFoundException extends RuntimeException {
    public AssetNotFoundException(String message) {
        super(message);
    }
}
