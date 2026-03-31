package pe.edu.vallegrande.movementservice.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.movementservice.application.dto.UserResponse;
import pe.edu.vallegrande.movementservice.domain.exception.ResourceNotFoundException;
import pe.edu.vallegrande.movementservice.domain.model.User;
import pe.edu.vallegrande.movementservice.infrastructure.adapters.output.persistence.UserRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public Flux<UserResponse> getUsersByMunicipality(UUID municipalityId) {
        log.info("Getting users for municipality: {}", municipalityId);
        return userRepository.findByMunicipalityIdAndStatus(municipalityId, "ACTIVE")
                .map(this::mapToResponse)
                .doOnComplete(() -> log.info("Retrieved users for municipality: {}", municipalityId));
    }

    public Mono<UserResponse> getUserById(UUID id, UUID municipalityId) {
        log.info("Getting user by id: {} for municipality: {}", id, municipalityId);
        return userRepository.findByIdAndMunicipalityId(id, municipalityId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("User not found with ID: " + id)))
                .map(this::mapToResponse);
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getUsername())
                .lastName("")
                .status(user.getStatus())
                .build();
    }
}
