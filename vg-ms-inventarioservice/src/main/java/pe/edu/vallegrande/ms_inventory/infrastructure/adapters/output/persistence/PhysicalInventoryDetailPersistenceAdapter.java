package pe.edu.vallegrande.ms_inventory.infrastructure.adapters.output.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import pe.edu.vallegrande.ms_inventory.application.ports.out.PhysicalInventoryDetailPersistencePort;
import pe.edu.vallegrande.ms_inventory.domain.model.PhysicalInventoryDetail;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PhysicalInventoryDetailPersistenceAdapter implements PhysicalInventoryDetailPersistencePort {

     private final PhysicalInventoryDetailRepository repository;

     @Override
     public Mono<PhysicalInventoryDetail> create(PhysicalInventoryDetail entity) {
          entity.setCreatedAt(java.time.LocalDateTime.now());
          entity.setUpdatedAt(java.time.LocalDateTime.now());
          return repository.save(entity);
     }

     @Override
     public Flux<PhysicalInventoryDetail> listAll() {
          return repository.findAll();
     }

     @Override
     public Flux<PhysicalInventoryDetail> listByInventoryId(UUID inventoryId) {
          return repository.findByInventoryId(inventoryId);
     }

     @Override
     public Mono<PhysicalInventoryDetail> update(UUID id, PhysicalInventoryDetail entity) {
          return repository.findById(id)
                    .flatMap(existing -> {
                         existing.setFoundStatus(entity.getFoundStatus());
                         existing.setActualConservationStatus(entity.getActualConservationStatus());
                         existing.setActualLocationId(entity.getActualLocationId());
                         existing.setActualResponsibleId(entity.getActualResponsibleId());
                         existing.setVerifiedBy(entity.getVerifiedBy());
                         existing.setVerificationDate(entity.getVerificationDate());
                         existing.setObservations(entity.getObservations());
                         existing.setRequiresAction(entity.getRequiresAction());
                         existing.setRequiredAction(entity.getRequiredAction());
                         existing.setPhotographs(entity.getPhotographs());
                         existing.setAdditionalEvidence(entity.getAdditionalEvidence());
                         existing.setPhysicalDifferences(entity.getPhysicalDifferences());
                         existing.setDocumentDifferences(entity.getDocumentDifferences());
                         existing.setUpdatedAt(java.time.LocalDateTime.now());
                         return repository.save(existing);
                    });
     }

     @Override
     public Mono<Void> deleteLogical(UUID id) {
          return repository.deleteById(id);
     }
}