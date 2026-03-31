package pe.edu.vallegrande.movementservice.infrastructure.adapters.input;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.movementservice.application.dto.UserResponse;
import pe.edu.vallegrande.movementservice.application.service.UserService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "API for managing users")
public class UserController {

    private final UserService userService;

    @GetMapping("/municipality/{municipalityId}")
    @Operation(summary = "Get users by municipality", description = "Retrieves all active users for a municipality")
    public Flux<UserResponse> getUsersByMunicipality(
            @Parameter(description = "Municipality ID") @PathVariable UUID municipalityId) {
        return userService.getUsersByMunicipality(municipalityId);
    }

    @GetMapping("/{id}/municipality/{municipalityId}")
    @Operation(summary = "Get user by ID", description = "Retrieves a user by its ID")
    public Mono<ResponseEntity<UserResponse>> getUserById(
            @Parameter(description = "User ID") @PathVariable UUID id,
            @Parameter(description = "Municipality ID") @PathVariable UUID municipalityId) {
        return userService.getUserById(id, municipalityId)
                .map(ResponseEntity::ok);
    }
}
