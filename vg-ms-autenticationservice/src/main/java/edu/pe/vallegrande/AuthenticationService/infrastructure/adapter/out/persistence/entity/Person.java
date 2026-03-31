package edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.out.persistence.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Entidad Person del sistema de autenticación */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("persons")
public class Person {

    @Id
    @Column("id")
    private UUID id;

    @Column("document_type_id")
    private Integer documentTypeId;

    @Column("document_number")
    private String documentNumber;

    @Column("person_type")
    private String personType; // N = Natural, J = Jurídica

    @Column("first_name")
    private String firstName;

    @Column("last_name")
    private String lastName;

    @Column("birth_date")
    private LocalDate birthDate;

    @Column("gender")
    private String gender; // M, F

    @Column("personal_phone")
    private String personalPhone;

    @Column("work_phone")
    private String workPhone;

    @Column("personal_email")
    private String personalEmail;

    @Column("address")
    private String address;

    @Column("municipal_code")
    private UUID municipalCode;

    @Column("status")
    private Boolean status;

    @Column("created_by")
    @CreatedBy
    private UUID createdBy;

    @Column("created_at")
    @CreatedDate
    private LocalDateTime createdAt;

    @Column("updated_by")
    @LastModifiedBy
    private UUID updatedBy;

    @Column("updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;
}