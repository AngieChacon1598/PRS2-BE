package pe.edu.vallegrande.configurationservice.infrastructure.adapters.input.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.configurationservice.application.dto.MunicipalityDTO;
import pe.edu.vallegrande.configurationservice.application.dto.MunicipalityRegistrationRequestDTO;
import pe.edu.vallegrande.configurationservice.application.dto.MunicipalityDetailResponseDTO;
import pe.edu.vallegrande.configurationservice.application.dto.ValidationResponseDTO;
import pe.edu.vallegrande.configurationservice.application.ports.input.MunicipalityUseCase;
import pe.edu.vallegrande.configurationservice.domain.model.Municipality;
import org.springframework.security.access.prepost.PreAuthorize;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/municipalities")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class MunicipalityController {

    private final MunicipalityUseCase municipalityUseCase;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ONBOARDING_MANAGER')")
    public Flux<MunicipalityDTO> findAll() {
        return municipalityUseCase.findAll()
                .map(this::mapToDTO);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ONBOARDING_MANAGER')")
    public Mono<MunicipalityDTO> findById(@PathVariable UUID id) {
        return municipalityUseCase.findById(id)
                .map(this::mapToDTO);
    }

    @GetMapping("/{id}/details")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ONBOARDING_MANAGER')")
    public Mono<MunicipalityDetailResponseDTO> getDetails(
            @PathVariable UUID id,
            @RequestHeader(value = "Authorization", required = false) String token) {
        return municipalityUseCase.getDetailById(id, token);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Mono<MunicipalityDTO> create(@RequestBody Municipality municipality) {
        return municipalityUseCase.create(municipality)
                .map(this::mapToDTO);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ONBOARDING_MANAGER')")
    public Mono<MunicipalityDTO> register(
            @RequestBody MunicipalityRegistrationRequestDTO request,
            @RequestHeader(value = "Authorization", required = false) String token) {
        return municipalityUseCase.register(request, token)
                .map(this::mapToDTO);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ONBOARDING_MANAGER')")
    public Mono<MunicipalityDTO> update(
            @PathVariable UUID id,
            @RequestBody MunicipalityRegistrationRequestDTO request,
            @RequestHeader("Authorization") String token) {
        return municipalityUseCase.update(id, request, token)
                .map(this::mapToDTO);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Mono<Void> delete(@PathVariable UUID id) {
        return municipalityUseCase.delete(id);
    }

    @GetMapping("/search/type/{type}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ONBOARDING_MANAGER')")
    public Flux<MunicipalityDTO> findByType(@PathVariable String type) {
        return municipalityUseCase.findByMunicipalityType(type)
                .map(this::mapToDTO);
    }

    @GetMapping("/search/department/{department}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ONBOARDING_MANAGER')")
    public Flux<MunicipalityDTO> findByDepartment(@PathVariable String department) {
        return municipalityUseCase.findByDepartment(department)
                .map(this::mapToDTO);
    }

    @GetMapping("/search/province/{province}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ONBOARDING_MANAGER')")
    public Flux<MunicipalityDTO> findByProvince(@PathVariable String province) {
        return municipalityUseCase.findByProvince(province)
                .map(this::mapToDTO);
    }

    @GetMapping("/validate/tax-id/{ruc}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ONBOARDING_MANAGER')")
    public Mono<ValidationResponseDTO> validateTaxId(@PathVariable String ruc,
            @RequestParam(required = false) UUID excludeId) {
        return municipalityUseCase.validateTaxId(ruc, excludeId);
    }

    @GetMapping("/validate/ubigeo-code/{ubigeo}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ONBOARDING_MANAGER')")
    public Mono<ValidationResponseDTO> validateUbigeo(@PathVariable String ubigeo,
            @RequestParam(required = false) UUID excludeId) {
        return municipalityUseCase.validateUbigeo(ubigeo, excludeId);
    }

    private MunicipalityDTO mapToDTO(Municipality municipality) {
        MunicipalityDTO dto = new MunicipalityDTO();
        dto.setId(municipality.getId());
        dto.setName(municipality.getName());
        dto.setRuc(municipality.getRuc());
        dto.setUbigeoCode(municipality.getUbigeoCode());
        dto.setMunicipalityType(municipality.getMunicipalityType());
        dto.setDepartment(municipality.getDepartment());
        dto.setProvince(municipality.getProvince());
        dto.setDistrict(municipality.getDistrict());
        dto.setAddress(municipality.getAddress());
        dto.setPhoneNumber(municipality.getPhoneNumber());
        dto.setMobileNumber(municipality.getMobileNumber());
        dto.setEmail(municipality.getEmail());
        dto.setWebsite(municipality.getWebsite());
        dto.setMayorName(municipality.getMayorName());
        dto.setIsActive(municipality.getIsActive());
        // Note: admin credentials are typically loaded via getDetails endpoint
        // but can be added here if available in the domain model
        dto.setCreatedAt(municipality.getCreatedAt());
        dto.setUpdatedAt(municipality.getUpdatedAt());
        return dto;
    }
}
