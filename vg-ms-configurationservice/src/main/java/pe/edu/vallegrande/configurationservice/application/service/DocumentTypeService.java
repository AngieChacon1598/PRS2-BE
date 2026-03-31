package pe.edu.vallegrande.configurationservice.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.configurationservice.domain.model.DocumentType;
import pe.edu.vallegrande.configurationservice.domain.exception.ResourceNotFoundException;
import pe.edu.vallegrande.configurationservice.infrastructure.adapters.output.persistence.repository.DocumentTypeRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class DocumentTypeService {

    private final DocumentTypeRepository repository;

    public Flux<DocumentType> getAllActive() {
        return repository.findAllActive();
    }

    public Flux<DocumentType> getAllInactive() {
        return repository.findAllInactive();
    }

    public Mono<DocumentType> getById(Integer id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("DocumentType not found with id: " + id)));
    }

    public Mono<DocumentType> create(DocumentType documentType) {
        documentType.setActive(true);
        return repository.save(documentType);
    }

    public Mono<DocumentType> update(Integer id, DocumentType dto) {
        return repository.updateDocumentType(
                id,
                dto.getCode(),
                dto.getDescription(),
                dto.getLength(),
                dto.getActive()
        ).then(repository.findById(id));
    }

    public Mono<DocumentType> partialUpdate(Integer id, DocumentType dto) {
        return repository.findById(id)
                .flatMap(existing -> {
                    if (dto.getCode() != null) existing.setCode(dto.getCode());
                    if (dto.getDescription() != null) existing.setDescription(dto.getDescription());
                    if (dto.getLength() != null) existing.setLength(dto.getLength());
                    if (dto.getActive() != null) existing.setActive(dto.getActive());
                    return repository.save(existing);
                });
    }

    public Mono<Void> delete(Integer id) {
        return repository.findById(id)
                .flatMap(dt -> {
                    dt.setActive(false);
                    return repository.save(dt);
                })
                .then();
    }

    public Mono<Void> restore(Integer id) {
        return repository.findById(id)
                .flatMap(dt -> {
                    dt.setActive(true);
                    return repository.save(dt);
                })
                .then();
    }
}