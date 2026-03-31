package edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.out.persistence.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad Permission para los permisos del sistema
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("permissions")
public class Permission {
    
    @Id
    private UUID id;
    
    @Column("module")
    private String module;
    
    @Column("action")
    private String action;
    
    @Column("resource")
    private String resource;

    @Column("display_name")
    private String displayName;
    
    @Column("description")
    private String description;
    
    @Column("created_by")
    private UUID createdBy;
    
    @Column("created_at")
    private LocalDateTime createdAt;
    
    @Column("status")
    private Boolean status;

    @Column("municipal_code")
    private UUID municipalCode;
}