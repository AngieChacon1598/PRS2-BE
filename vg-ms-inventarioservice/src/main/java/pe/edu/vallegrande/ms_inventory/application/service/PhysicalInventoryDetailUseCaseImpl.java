package pe.edu.vallegrande.ms_inventory.application.service;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.edu.vallegrande.ms_inventory.application.ports.in.PhysicalInventoryDetailUseCase;
import pe.edu.vallegrande.ms_inventory.application.ports.out.PhysicalInventoryDetailPersistencePort;
import pe.edu.vallegrande.ms_inventory.domain.model.PhysicalInventoryDetail;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PhysicalInventoryDetailUseCaseImpl implements PhysicalInventoryDetailUseCase {

    private final PhysicalInventoryDetailPersistencePort persistencePort;

    @Override
    public Flux<PhysicalInventoryDetail> listAll() {
        return persistencePort.listAll();
    }

    @Override
    public Flux<PhysicalInventoryDetail> listByInventoryId(UUID inventoryId) {
        return persistencePort.listByInventoryId(inventoryId);
    }

    @Override
    @Transactional
    public Mono<PhysicalInventoryDetail> create(PhysicalInventoryDetail detail) {
        detail.setCreatedAt(LocalDateTime.now());
        detail.setUpdatedAt(LocalDateTime.now());

        if (detail.getPhotographs() == null)
            detail.setPhotographs(JsonNodeFactory.instance.arrayNode());
        if (detail.getAdditionalEvidence() == null)
            detail.setAdditionalEvidence(JsonNodeFactory.instance.arrayNode());
        if (detail.getRequiresAction() == null)
            detail.setRequiresAction(false);

        return persistencePort.create(detail);
    }

    @Override
    @Transactional
    public Mono<PhysicalInventoryDetail> update(UUID id, PhysicalInventoryDetail updated) {
        return persistencePort.update(id, updated);
    }

    @Override
    @Transactional
    public Mono<Void> deleteLogical(UUID id) {
        return persistencePort.deleteLogical(id);
    }
}
