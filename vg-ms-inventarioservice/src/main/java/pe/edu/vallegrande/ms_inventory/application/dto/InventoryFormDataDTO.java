package pe.edu.vallegrande.ms_inventory.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryFormDataDTO {
    private List<AreaDTO> areas;
    private List<CategoryDTO> categories;
    private List<LocationDTO> locations;
    private List<UserDTO> users;
}
