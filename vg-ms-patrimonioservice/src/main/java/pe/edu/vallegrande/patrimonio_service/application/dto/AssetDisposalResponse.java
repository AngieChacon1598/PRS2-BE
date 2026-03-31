package pe.edu.vallegrande.patrimonio_service.application.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AssetDisposalResponse {
    private UUID id;
    private UUID municipalityId;
    private String fileNumber;
    private String disposalType;
    private String disposalReason;
    private String reasonDescription;
    private LocalDate requestDate;
    private LocalDate technicalEvaluationDate;
    private LocalDate resolutionDate;
    private LocalDate physicalRemovalDate;
    private UUID technicalReportAuthorId; // Nuevo: quién elabora el informe técnico
    private String technicalReport;
    private UUID approvedById; // Nuevo: quién aprueba o rechaza (administrador de finanzas)
    private LocalDate approvalDate;
    private String fileStatus;
    private String resolutionNumber;
    private String supportingDocuments;
    private String observations;
    private Boolean requiresDestruction;
    private Boolean allowsDonation;
    private BigDecimal recoverableValue;
    private UUID requestedBy;
    private LocalDateTime createdAt;
    private UUID updatedBy;
    private LocalDateTime updatedAt;
}
