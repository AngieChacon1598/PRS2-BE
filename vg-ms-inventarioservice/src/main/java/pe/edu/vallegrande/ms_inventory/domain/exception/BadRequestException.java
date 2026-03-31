package pe.edu.vallegrande.ms_inventory.domain.exception;

public class BadRequestException extends RuntimeException {

     public BadRequestException(String message) {
          super(message);
     }
}
