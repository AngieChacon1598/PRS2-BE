package pe.edu.vallegrande.configurationservice.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.configurationservice.application.dto.ConfiguracionTenantDTO;
import pe.edu.vallegrande.configurationservice.domain.model.ConfiguracionTenant;
import pe.edu.vallegrande.configurationservice.infrastructure.adapters.output.persistence.repository.ConfiguracionTenantRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConfiguracionTenantService {

    private final ConfiguracionTenantRepository repository;

    public Flux<ConfiguracionTenantDTO> findAll() {
        return repository.findAll().map(this::toDTO);
    }

    public Mono<ConfiguracionTenantDTO> findById(UUID id) {
        return repository.findById(id).map(this::toDTO);
    }

    public Mono<ConfiguracionTenantDTO> findByMunicipalityId(UUID municipalityId) {
        return repository.findByMunicipalityId(municipalityId).map(this::toDTO);
    }

    public Flux<ConfiguracionTenantDTO> findByActive(Boolean active) {
        return repository.findByActive(active).map(this::toDTO);
    }

    public Mono<ConfiguracionTenantDTO> save(ConfiguracionTenantDTO dto) {
        ConfiguracionTenant entity = toEntity(dto);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        return repository.save(entity).map(this::toDTO);
    }

    public Mono<ConfiguracionTenantDTO> update(UUID id, ConfiguracionTenantDTO dto) {
        return repository.findById(id)
                .flatMap(existing -> {
                    ConfiguracionTenant entity = toEntity(dto);
                    entity.setId(id);
                    entity.setCreatedAt(existing.getCreatedAt());
                    entity.setUpdatedAt(LocalDateTime.now());
                    return repository.save(entity);
                })
                .map(this::toDTO);
    }

    public Mono<Void> deleteById(UUID id) {
        return repository.deleteById(id);
    }

    private ConfiguracionTenantDTO toDTO(ConfiguracionTenant entity) {
        ConfiguracionTenantDTO dto = new ConfiguracionTenantDTO();
        dto.setId(entity.getId());
        dto.setMunicipalityId(entity.getMunicipalityId());
        dto.setSystemName(entity.getSystemName());
        dto.setLogoUrl(entity.getLogoUrl());
        dto.setThemeColors(entity.getThemeColors());
        dto.setReportsConfiguration(entity.getReportsConfiguration());
        dto.setBusinessParameters(entity.getBusinessParameters());
        dto.setDefaultCurrency(entity.getDefaultCurrency());
        dto.setTimezone(entity.getTimezone());
        dto.setActive(entity.getActive());
        return dto;
    }

    private ConfiguracionTenant toEntity(ConfiguracionTenantDTO dto) {
        ConfiguracionTenant entity = new ConfiguracionTenant();
        entity.setId(dto.getId());
        entity.setMunicipalityId(dto.getMunicipalityId());
        entity.setSystemName(dto.getSystemName());
        entity.setLogoUrl(dto.getLogoUrl());
        entity.setThemeColors(dto.getThemeColors());
        entity.setReportsConfiguration(dto.getReportsConfiguration());
        entity.setBusinessParameters(dto.getBusinessParameters());
        entity.setDefaultCurrency(dto.getDefaultCurrency());
        entity.setTimezone(dto.getTimezone());
        entity.setActive(dto.getActive());
        return entity;
    }
}
