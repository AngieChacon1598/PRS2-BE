package pe.edu.vallegrande.ms_inventory.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocationDTO {
    private UUID id;
    private String name;
    private Boolean active;
    private String status;
}
