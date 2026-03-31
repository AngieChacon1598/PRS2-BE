package pe.edu.vallegrande.configurationservice.domain.exception;

public class DuplicateDocumentException extends RuntimeException {
    
    public DuplicateDocumentException(String message) {
        super(message);
    }
    
    public DuplicateDocumentException(Integer documentTypeId, String numeroDocumento) {
        super(String.format("Ya existe un proveedor registrado con el tipo de documento %d y número %s", 
            documentTypeId, numeroDocumento));
    }
}
