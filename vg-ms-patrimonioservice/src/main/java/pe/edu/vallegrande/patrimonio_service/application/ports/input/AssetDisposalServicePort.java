package pe.edu.vallegrande.patrimonio_service.application.ports.input;

import pe.edu.vallegrande.patrimonio_service.application.dto.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface AssetDisposalServicePort {
    
    Mono<AssetDisposalResponse> create(AssetDisposalRequest request);
    
    Mono<AssetDisposalResponse> getById(UUID id);
    
    Flux<AssetDisposalResponse> getAll();
    
    Flux<AssetDisposalResponse> getByStatus(String fileStatus);
    
    Mono<AssetDisposalResponse> getByFileNumber(String fileNumber);
    
    Flux<AssetDisposalResponse> getByRequestedBy(UUID requestedBy);
    
    Mono<AssetDisposalResponse> assignCommittee(UUID id, AssignCommitteeRequest request);
    
    Mono<AssetDisposalResponse> resolve(UUID id, ResolveDisposalRequest request);
    
    Mono<AssetDisposalResponse> cancel(UUID id, UUID cancelledBy);
    
    Mono<AssetDisposalResponse> finalize(UUID id);
    
    Mono<Void> delete(UUID id);
}
