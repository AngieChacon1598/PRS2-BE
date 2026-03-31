package pe.edu.vallegrande.patrimonio_service.application.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AssetRequest {
    private UUID municipalityId;
    private String assetCode;
    private String internalCode;
    private String sbnCode;
    private String description;
    private UUID categoryId;
    private UUID subcategoryId;
    private String brand;
    private String model;
    private String serialNumber;
    private Integer usefulLife;
    private String assetPlate;
    private String qrCode;
    private String barcode;
    private String rfidTag;
    private String color;
    private String dimensions;
    private BigDecimal weight;
    private String material;
    private UUID supplierId;
    private LocalDate acquisitionDate;
    private String acquisitionType;
    private String invoiceNumber;
    private String purchaseOrderNumber;
    private String pecosaNumber;
    private BigDecimal acquisitionValue;
    private String currency;
    private BigDecimal currentValue;
    private BigDecimal residualValue;
    private BigDecimal accumulatedDepreciation;
    private String assetStatus;
    private String conservationStatus;
    private UUID currentLocationId;
    private UUID currentResponsibleId;
    private UUID currentAreaId;
    private LocalDateTime entryDate;
    private LocalDate lastInventoryDate;
    private LocalDate nextDepreciationDate;
    private LocalDate warrantyExpirationDate;
    private String observations;
    private String technicalSpecifications;
    private String imageUrl;
    private String attachedDocuments;
    private String customFields;
    private Boolean isInventoriable;
    private Boolean requiresMaintenance;
    private Boolean isDepreciable;
    private UUID createdBy;
    // SBN normative fields (fase 2)
    private UUID finalUserId;
    private LocalDate altaDate;
    private String altaDocType;
    private String altaDocNumber;
    private String accountCode;
}
