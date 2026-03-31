package pe.edu.vallegrande.configurationservice.domain.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("municipalities")
public class Municipality {
    @Id
    @Column("id")
    private UUID id;

    @Column("name")
    @NotNull(message = "Name is required")
    private String name;

    @Column("ruc")
    @NotNull(message = "Tax ID (RUC) is required")
    @Size(min = 11, max = 11, message = "Tax ID (RUC) must be 11 digits")
    private String ruc;

    @Column("ubigeo_code")
    @NotNull(message = "Ubigeo code is required")
    @Size(min = 6, max = 6, message = "Ubigeo code must be 6 digits")
    private String ubigeoCode;

    @Column("municipality_type")
    @NotNull(message = "Municipality type is required")
    private String municipalityType = "DISTRICT";

    @Column("department")
    @NotNull(message = "Department is required")
    private String department;

    @Column("province")
    @NotNull(message = "Province is required")
    private String province;

    @Column("district")
    @NotNull(message = "District is required")
    private String district;

    @Column("address")
    private String address;

    @Column("phone_number")
    @Size(max = 50, message = "Phone number must not exceed 50 characters")
    private String phoneNumber;

    @Column("mobile_number")
    @Size(max = 20, message = "Mobile number must not exceed 20 characters")
    private String mobileNumber;

    @Column("email")
    @Email(message = "Email must be valid")
    private String email;

    @Column("website")
    private String website;

    @Column("mayor_name")
    private String mayorName;

    @Column("is_active")
    private Boolean isActive = true;

    @CreatedDate
    @Column("created_at")
    private ZonedDateTime createdAt;

    @LastModifiedDate
    @Column("updated_at")
    private ZonedDateTime updatedAt;
}
