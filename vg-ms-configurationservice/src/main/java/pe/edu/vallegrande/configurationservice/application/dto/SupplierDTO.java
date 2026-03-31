package pe.edu.vallegrande.configurationservice.application.dto;

import lombok.Data;

@Data
public class SupplierDTO {
    private Integer documentTypesId;
    private String numeroDocumento;
    private String legalName;
    private String tradeName;
    private String address;
    private String phone;
    private String email;
    private String website;
    private String mainContact;
    private String companyType;
    private Boolean isStateProvider;
    private String classification;
}
