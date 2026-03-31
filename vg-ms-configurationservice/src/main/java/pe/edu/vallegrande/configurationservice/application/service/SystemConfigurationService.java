package pe.edu.vallegrande.configurationservice.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.configurationservice.domain.model.SystemConfiguration;
import pe.edu.vallegrande.configurationservice.infrastructure.adapters.output.persistence.repository.SystemConfigurationRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SystemConfigurationService {

    private final SystemConfigurationRepository repository;

    public Flux<SystemConfiguration> getAll() {
        return repository.findAll()
                .sort((a, b) -> a.getCategory().compareToIgnoreCase(b.getCategory()));
    }

    public Mono<SystemConfiguration> create(SystemConfiguration config) {
        config.setCreatedAt(LocalDateTime.now());
        config.setUpdatedAt(LocalDateTime.now());

        if (config.getIsEditable() == null) config.setIsEditable(true);
        if (config.getRequiresRestart() == null) config.setRequiresRestart(false);
        if (config.getIsSensitive() == null) config.setIsSensitive(false);

        return repository.save(config);
    }

    public Mono<SystemConfiguration> update(UUID id, SystemConfiguration newData) {
        return repository.findById(id)
                .flatMap(existing -> {
                    existing.setMunicipalityId(newData.getMunicipalityId());
                    existing.setCategory(newData.getCategory());
                    existing.setKey(newData.getKey());
                    existing.setValue(newData.getValue());
                    existing.setDataType(newData.getDataType());
                    existing.setDescription(newData.getDescription());
                    existing.setIsEditable(newData.getIsEditable());
                    existing.setRequiresRestart(newData.getRequiresRestart());
                    existing.setIsSensitive(newData.getIsSensitive());
                    existing.setMinimumValue(newData.getMinimumValue());
                    existing.setMaximumValue(newData.getMaximumValue());
                    existing.setAllowedValues(newData.getAllowedValues());
                    existing.setValidationPattern(newData.getValidationPattern());
                    existing.setUpdatedAt(LocalDateTime.now());
                    return repository.save(existing);
                });
    }

    public Mono<Void> softDelete(UUID id) {
        return repository.findById(id)
                .flatMap(config -> {
                    config.setIsEditable(false);
                    config.setUpdatedAt(LocalDateTime.now());
                    return repository.save(config);
                })
                .then();
    }

    public Mono<Void> restore(UUID id) {
        return repository.findById(id)
                .flatMap(config -> {
                    config.setIsEditable(true);
                    config.setUpdatedAt(LocalDateTime.now());
                    return repository.save(config);
                })
                .then();
    }
}