package pe.edu.vallegrande.ms_inventory.domain.exception;

public class ResourceNotFoundException extends RuntimeException {
     public ResourceNotFoundException(String message) {
          super(message);
     }
}
