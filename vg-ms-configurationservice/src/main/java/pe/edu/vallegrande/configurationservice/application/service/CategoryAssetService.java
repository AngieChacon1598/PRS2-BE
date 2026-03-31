package pe.edu.vallegrande.configurationservice.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.configurationservice.domain.model.CategoryAsset;
import pe.edu.vallegrande.configurationservice.infrastructure.adapters.output.persistence.repository.CategoryAssetRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryAssetService {

    private final CategoryAssetRepository repository;

    public Flux<CategoryAsset> getAll() {
        return repository.findAll()
                .sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
    }

    public Flux<CategoryAsset> getAllActive() {
        return repository.findByActiveTrueOrderByNameAsc();
    }

    public Flux<CategoryAsset> getAllInactive() {
        return repository.findByActiveFalseOrderByNameAsc();
    }

    public Mono<CategoryAsset> create(CategoryAsset category) {
        category.setActive(true);
        category.setCreatedAt(LocalDateTime.now());
        return repository.save(category);
    }

    public Mono<CategoryAsset> update(UUID id, CategoryAsset newData) {
        return repository.findById(id)
                .flatMap(existing -> {
                    existing.setMunicipalityId(newData.getMunicipalityId());
                    existing.setCategoryCode(newData.getCategoryCode());
                    existing.setName(newData.getName());
                    existing.setDescription(newData.getDescription());
                    existing.setParentCategoryId(newData.getParentCategoryId());
                    existing.setLevel(newData.getLevel());
                    existing.setAccountingAccount(newData.getAccountingAccount());
                    existing.setAnnualDepreciation(newData.getAnnualDepreciation());
                    existing.setUsefulLifeYears(newData.getUsefulLifeYears());
                    existing.setResidualValuePct(newData.getResidualValuePct());
                    existing.setRequiresSerial(newData.getRequiresSerial());
                    existing.setRequiresPlate(newData.getRequiresPlate());
                    existing.setIsInventoriable(newData.getIsInventoriable());
                    existing.setUpdatedAt(LocalDateTime.now());
                    return repository.save(existing);
                });
    }

    public Mono<CategoryAsset> softDelete(UUID id) {
        return repository.findById(id)
                .flatMap(category -> {
                    category.setActive(false);
                    category.setUpdatedAt(LocalDateTime.now());
                    return repository.save(category);
                });
    }

    public Mono<CategoryAsset> restore(UUID id) {
        return repository.findById(id)
                .flatMap(category -> {
                    category.setActive(true);
                    category.setUpdatedAt(LocalDateTime.now());
                    return repository.save(category);
                });
    }
}