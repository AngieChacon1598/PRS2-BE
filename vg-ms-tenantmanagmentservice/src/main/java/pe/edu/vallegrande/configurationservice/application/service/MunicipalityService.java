package pe.edu.vallegrande.configurationservice.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.configurationservice.application.dto.MunicipalityRegistrationRequestDTO;
import pe.edu.vallegrande.configurationservice.application.dto.MunicipalityDetailResponseDTO;
import pe.edu.vallegrande.configurationservice.application.dto.PersonRequestDto;
import pe.edu.vallegrande.configurationservice.application.dto.UserCreateRequestDto;
import pe.edu.vallegrande.configurationservice.application.dto.UserUpdateRequestDto;
import pe.edu.vallegrande.configurationservice.application.dto.TenantOnboardingRequestDto;
import pe.edu.vallegrande.configurationservice.application.dto.ValidationResponseDTO;
import pe.edu.vallegrande.configurationservice.application.ports.input.MunicipalityUseCase;
import pe.edu.vallegrande.configurationservice.application.ports.output.AuthClientPort;
import pe.edu.vallegrande.configurationservice.application.ports.output.MunicipalityPersistencePort;
import pe.edu.vallegrande.configurationservice.domain.exception.DuplicateKeyException;
import pe.edu.vallegrande.configurationservice.domain.exception.MunicipalityNotFoundException;
import pe.edu.vallegrande.configurationservice.domain.exception.ValidationException;
import pe.edu.vallegrande.configurationservice.domain.model.Municipality;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MunicipalityService implements MunicipalityUseCase {

    private final MunicipalityPersistencePort persistencePort;
    private final AuthClientPort authClientPort;

    @Value("${app.auth.system-username:dgonzales}")
    private String systemUsername;

    @Value("${app.auth.system-password:SuperAdmin}")
    private String systemPassword;

    @Override
    public Flux<Municipality> findAll() {
        return persistencePort.findAll();
    }

    @Override
    public Mono<Municipality> findById(UUID id) {
        return persistencePort.findById(id)
                .switchIfEmpty(Mono.error(new MunicipalityNotFoundException(id)));
    }

    @Override
    public Mono<MunicipalityDetailResponseDTO> getDetailById(UUID id, String token) {
        Mono<String> tokenMono = (token != null && token.startsWith("Bearer ")) 
            ? Mono.just(token) 
            : authClientPort.login(systemUsername, systemPassword);

        return findById(id)
                .flatMap(muni -> tokenMono.flatMap(validToken -> 
                        authClientPort.getUserByMunicipalCode(id, validToken)
                                .map(user -> MunicipalityDetailResponseDTO.builder()
                                        .municipality(muni)
                                        .adminUsername(user.getUsername())
                                        .adminPasswordHash(user.getPasswordHash())
                                        .build())
                                .defaultIfEmpty(MunicipalityDetailResponseDTO.builder()
                                        .municipality(muni)
                                        .adminUsername("No asignado")
                                        .adminPasswordHash("No asignado")
                                        .build())
                                .onErrorResume(error -> {
                                    log.warn("Could not retrieve admin user for municipality {}: {}", id, error.getMessage());
                                    return Mono.just(MunicipalityDetailResponseDTO.builder()
                                            .municipality(muni)
                                            .adminUsername("No asignado")
                                            .adminPasswordHash("No asignado")
                                            .build());
                                })
                ));
    }

    @Override
    public Mono<Municipality> create(Municipality municipality) {
        log.info("Starting municipality creation: {}", municipality);

        validateMunicipality(municipality);

        return persistencePort.existsByUbigeoCode(municipality.getUbigeoCode())
                .flatMap(ubigeoExists -> {
                    if (ubigeoExists) {
                        return Mono.error(new DuplicateKeyException(
                                "A municipality with this ubigeo code already exists: "
                                        + municipality.getUbigeoCode()));
                    }
                    return persistencePort.existsByRuc(municipality.getRuc());
                })
                .flatMap(rucExists -> {
                    if (rucExists) {
                        return Mono.error(new DuplicateKeyException(
                                "A municipality with this tax ID (RUC) already exists: " + municipality.getRuc()));
                    }

                    // Set default values and timestamps
                    municipality.setCreatedAt(ZonedDateTime.now());
                    municipality.setUpdatedAt(ZonedDateTime.now());
                    municipality.setIsActive(municipality.getIsActive() != null ? municipality.getIsActive() : true);
                    municipality.setMunicipalityType(
                            municipality.getMunicipalityType() != null ? municipality.getMunicipalityType()
                                    : "DISTRICT");

                    return persistencePort.save(municipality)
                            .doOnSuccess(saved -> log.info("Municipality created successfully: {}", saved));
                });
    }

    private void validateMunicipality(Municipality municipality) {
        if (municipality.getName() == null || municipality.getName().trim().isEmpty()) {
            throw new ValidationException("Name is required");
        }
        if (municipality.getRuc() == null || municipality.getRuc().trim().isEmpty()) {
            throw new ValidationException("Tax ID (RUC) is required");
        }
        if (municipality.getUbigeoCode() == null || municipality.getUbigeoCode().trim().isEmpty()) {
            throw new ValidationException("Ubigeo code is required");
        }
    }

    @Override
    public Mono<Municipality> register(MunicipalityRegistrationRequestDTO request, String token) {
        log.info("Starting unified registration for municipality: {}", request.getMunicipality().getName());
        Municipality municipality = request.getMunicipality();

        return create(municipality)
                .flatMap(savedMuni -> {
                    log.info("Municipality saved with ID: {}. Proceeding with Auth onboarding.", savedMuni.getId());

                    // System-to-System login to get SuperAdmin token for automated creation
                    return authClientPort.login(systemUsername, systemPassword)
                            .flatMap(systemToken -> {
                                TenantOnboardingRequestDto onboardingDto = TenantOnboardingRequestDto.builder()
                                        .adminUsername(request.getAdminUsername())
                                        .adminPassword(request.getAdminPassword())
                                        .municipalCode(savedMuni.getId())
                                        .authorityName(savedMuni.getName())
                                        .email(savedMuni.getEmail())
                                        .build();

                                return authClientPort.onboardTenant(onboardingDto, systemToken)
                                        .thenReturn(savedMuni);
                            });
                })
                .onErrorResume(error -> {
                    log.error("CRITICAL ERROR in registration orchestration: {}", error.getMessage(), error);
                    return Mono.error(error);
                });
    }

    private PersonRequestDto mapToPersonRequest(Municipality muni) {
        PersonRequestDto dto = new PersonRequestDto();
        dto.setDocumentTypeId(1); // DNI por defecto
        dto.setDocumentNumber(muni.getRuc() != null && !muni.getRuc().isEmpty() ? muni.getRuc() : "00000000");
        dto.setPersonType("N");

        String mayorName = (muni.getMayorName() != null && !muni.getMayorName().trim().isEmpty())
                ? muni.getMayorName().trim()
                : "Administrador Municipal";

        String[] nameParts = mayorName.split("\\s+");
        if (nameParts.length > 1) {
            dto.setFirstName(nameParts[0]);
            String lastName = mayorName.substring(nameParts[0].length()).trim();
            // Asegurar que el apellido tenga al menos 2 caracteres
            dto.setLastName(lastName.length() >= 2 ? lastName : lastName + " Admin");
        } else {
            dto.setFirstName(mayorName);
            // El apellido no puede ser solo un punto o muy corto
            dto.setLastName("Administrador");
        }

        dto.setBirthDate(LocalDate.of(1990, 1, 1));
        dto.setGender("M");
        dto.setPersonalPhone((muni.getPhoneNumber() != null && !muni.getPhoneNumber().trim().isEmpty())
                ? muni.getPhoneNumber()
                : "999999999");
        dto.setPersonalEmail((muni.getEmail() != null && !muni.getEmail().trim().isEmpty())
                ? muni.getEmail()
                : "admin@vallegrande.edu.pe");
        dto.setAddress((muni.getAddress() != null && !muni.getAddress().trim().isEmpty())
                ? muni.getAddress()
                : "Calle Real s/n");
        return dto;
    }

    @Override
    public Mono<Municipality> update(UUID id, MunicipalityRegistrationRequestDTO request, String token) {
        Mono<String> tokenMono = (token != null && token.startsWith("Bearer ")) 
            ? Mono.just(token) 
            : authClientPort.login(systemUsername, systemPassword);
        Municipality municipality = request.getMunicipality();
        return persistencePort.findById(id)
                .switchIfEmpty(Mono.error(new MunicipalityNotFoundException(id)))
                .flatMap(existingMunicipality -> {
                    log.info("Updating municipality: {} with data: {}", id, municipality);

                    if (municipality.getName() != null && !municipality.getName().isEmpty()) {
                        existingMunicipality.setName(municipality.getName());
                    }
                    if (municipality.getRuc() != null && !municipality.getRuc().isEmpty()) {
                        existingMunicipality.setRuc(municipality.getRuc());
                    }
                    if (municipality.getUbigeoCode() != null && !municipality.getUbigeoCode().isEmpty()) {
                        existingMunicipality.setUbigeoCode(municipality.getUbigeoCode());
                    }
                    if (municipality.getMunicipalityType() != null && !municipality.getMunicipalityType().isEmpty()) {
                        existingMunicipality.setMunicipalityType(municipality.getMunicipalityType());
                    }
                    if (municipality.getDepartment() != null && !municipality.getDepartment().isEmpty()) {
                        existingMunicipality.setDepartment(municipality.getDepartment());
                    }
                    if (municipality.getProvince() != null && !municipality.getProvince().isEmpty()) {
                        existingMunicipality.setProvince(municipality.getProvince());
                    }
                    if (municipality.getDistrict() != null && !municipality.getDistrict().isEmpty()) {
                        existingMunicipality.setDistrict(municipality.getDistrict());
                    }
                    if (municipality.getAddress() != null && !municipality.getAddress().isEmpty()) {
                        existingMunicipality.setAddress(municipality.getAddress());
                    }
                    if (municipality.getPhoneNumber() != null) {
                        existingMunicipality.setPhoneNumber(municipality.getPhoneNumber());
                    }
                    if (municipality.getMobileNumber() != null) {
                        existingMunicipality.setMobileNumber(municipality.getMobileNumber());
                    }
                    if (municipality.getEmail() != null) {
                        existingMunicipality.setEmail(municipality.getEmail());
                    }
                    if (municipality.getWebsite() != null) {
                        existingMunicipality.setWebsite(municipality.getWebsite());
                    }
                    if (municipality.getMayorName() != null) {
                        existingMunicipality.setMayorName(municipality.getMayorName());
                    }
                    if (municipality.getIsActive() != null) {
                        existingMunicipality.setIsActive(municipality.getIsActive());
                    }

                    existingMunicipality.setUpdatedAt(ZonedDateTime.now());

                    log.info("Saving updated municipality: {}", existingMunicipality);
                    return tokenMono.flatMap(validToken -> persistencePort.save(existingMunicipality)
                            .flatMap(savedMuni -> {
                                if (request.getAdminUsername() == null || request.getAdminUsername().isEmpty()) {
                                    return Mono.just(savedMuni);
                                }
                                return handleAdminCredentialUpdate(savedMuni, request, validToken)
                                        .thenReturn(savedMuni);
                            })
                    );
                });
    }

    private Mono<Void> handleAdminCredentialUpdate(Municipality muni, MunicipalityRegistrationRequestDTO request, String validToken) {
        return authClientPort.getUserByMunicipalCode(muni.getId(), validToken)
                .onErrorResume(e -> {
                    log.info("No existing admin user found for municipality {} during update. Will create one using onboarding.",
                            muni.getId());
                    return Mono.empty();
                })
                .flatMap(user -> {
                    // Update existing user
                    UserUpdateRequestDto updateDto = UserUpdateRequestDto.builder()
                            .username(request.getAdminUsername())
                            .password(request.getAdminPassword() != null && !request.getAdminPassword().isEmpty()
                                    ? request.getAdminPassword()
                                    : null)
                            .personId(user.getPersonId())
                            .status("ACTIVE")
                            .build();
                    return authClientPort.updateUser(user.getId(), updateDto, validToken);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    // Create new user if not exists using the new onboarding endpoint
                    TenantOnboardingRequestDto onboardingDto = TenantOnboardingRequestDto.builder()
                            .adminUsername(request.getAdminUsername())
                            .adminPassword(request.getAdminPassword())
                            .municipalCode(muni.getId())
                            .authorityName(muni.getName())
                            .email(muni.getEmail())
                            .build();
                    return authClientPort.onboardTenant(onboardingDto, validToken);
                }))
                .then();
    }

    @Override
    public Mono<Void> delete(UUID id) {
        return persistencePort.deleteById(id);
    }

    @Override
    public Flux<Municipality> findByMunicipalityType(String municipalityType) {
        return persistencePort.findByMunicipalityType(municipalityType);
    }

    @Override
    public Flux<Municipality> findByDepartment(String department) {
        return persistencePort.findByDepartment(department);
    }

    @Override
    public Flux<Municipality> findByProvince(String province) {
        return persistencePort.findByProvince(province);
    }

    @Override
    public Mono<ValidationResponseDTO> validateTaxId(String ruc, UUID excludeId) {
        return persistencePort.existsByRuc(ruc)
                .flatMap(exists -> {
                    if (exists) {
                        if (excludeId != null) {
                            return findById(excludeId)
                                    .map(muni -> muni.getRuc().equals(ruc))
                                    .map(sameRuc -> ValidationResponseDTO.builder()
                                            .valid(sameRuc)
                                            .message(
                                                    sameRuc ? null : "El RUC ya estÃ¡ registrado por otra municipalidad")
                                            .build());
                        }
                        return Mono.just(ValidationResponseDTO.builder()
                                .valid(false)
                                .message("El RUC ya estÃ¡ registrado")
                                .build());
                    }
                    return Mono.just(ValidationResponseDTO.builder().valid(true).build());
                });
    }

    @Override
    public Mono<ValidationResponseDTO> validateUbigeo(String ubigeo, UUID excludeId) {
        return persistencePort.existsByUbigeoCode(ubigeo)
                .flatMap(exists -> {
                    if (exists) {
                        if (excludeId != null) {
                            return findById(excludeId)
                                    .map(muni -> muni.getUbigeoCode().equals(ubigeo))
                                    .map(sameUbigeo -> ValidationResponseDTO.builder()
                                            .valid(sameUbigeo)
                                            .message(sameUbigeo ? null
                                                    : "El cÃ³digo de ubigeo ya estÃ¡ registrado por otra municipalidad")
                                            .build());
                        }
                        return Mono.just(ValidationResponseDTO.builder()
                                .valid(false)
                                .message("El cÃ³digo de ubigeo ya estÃ¡ registrado")
                                .build());
                    }
                    return Mono.just(ValidationResponseDTO.builder().valid(true).build());
                });
    }
}


