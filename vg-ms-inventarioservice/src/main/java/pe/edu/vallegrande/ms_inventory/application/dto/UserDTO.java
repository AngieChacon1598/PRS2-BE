package pe.edu.vallegrande.ms_inventory.application.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class UserDTO {
    private UUID id;
    private String nombre;
    private String email;
    private String username;
    private Boolean active;
    private String status;
}
