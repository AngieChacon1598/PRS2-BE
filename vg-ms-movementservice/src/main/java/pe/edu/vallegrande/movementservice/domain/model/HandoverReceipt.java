package pe.edu.vallegrande.movementservice.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("handover_receipts")
public class HandoverReceipt {

    @Id
    private UUID id;

    @Column("municipality_id")
    private UUID municipalityId;

    @Column("receipt_number")
    private String receiptNumber;

    @Column("movement_id")
    private UUID movementId;

    @Column("delivering_responsible_id")
    private UUID deliveringResponsibleId;

    @Column("receiving_responsible_id")
    private UUID receivingResponsibleId;

    @Column("witness_1_id")
    private UUID witness1Id;

    @Column("witness_2_id")
    private UUID witness2Id;

    @Column("receipt_date")
    private LocalDate receiptDate;

    @Column("delivery_signature_date")
    private LocalDateTime deliverySignatureDate;

    @Column("reception_signature_date")
    private LocalDateTime receptionSignatureDate;

    @Column("receipt_status")
    private String receiptStatus;

    @Column("delivery_observations")
    private String deliveryObservations;

    @Column("reception_observations")
    private String receptionObservations;

    @Column("special_conditions")
    private String specialConditions;

    @Column("digital_signatures")
    private String digitalSignatures;

    @Column("generated_by")
    private UUID generatedBy;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;
}
