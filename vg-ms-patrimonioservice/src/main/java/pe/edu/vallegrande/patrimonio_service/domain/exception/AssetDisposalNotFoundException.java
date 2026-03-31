package pe.edu.vallegrande.patrimonio_service.domain.exception;

public class AssetDisposalNotFoundException extends RuntimeException {
    public AssetDisposalNotFoundException(String message) {
        super(message);
    }
}
