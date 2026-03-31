package edu.pe.vallegrande.AuthenticationService.application.service;

import edu.pe.vallegrande.AuthenticationService.domain.exception.ResourceNotFoundException;
import edu.pe.vallegrande.AuthenticationService.domain.model.user.UserAccount;
import edu.pe.vallegrande.AuthenticationService.domain.ports.out.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserPort userPort;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private CurrentUserPort currentUserPort;
    @Mock
    private ExternalAuthPort externalAuthPort;
    @Mock
    private PersonPort personPort;
    @Mock
    private RolePort rolePort;
    @Mock
    private UserRolePort userRolePort;
    @Mock
    private ConfigServiceClientPort configServiceClientPort;

    @InjectMocks
    private UserServiceImpl userService;

    private UserAccount testUser;
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userService, "defaultRealm", "testrealm");
        testUser = UserAccount.builder()
                .id(userId)
                .username("testuser")
                .status("ACTIVE")
                .municipalCode(UUID.randomUUID())
                .positionId(UUID.randomUUID())
                .areaId(UUID.randomUUID())
                .build();
    }

    @Test
    void createUser_Success() {
        edu.pe.vallegrande.AuthenticationService.domain.model.user.CreateUserCommand command = 
            edu.pe.vallegrande.AuthenticationService.domain.model.user.CreateUserCommand.builder()
                .username("testuser")
                .password("testpass")
                .personId(UUID.randomUUID())
                .areaId(UUID.randomUUID())
                .positionId(UUID.randomUUID())
                .build();

        when(userPort.existsByUsername(command.getUsername())).thenReturn(Mono.just(false));
        when(currentUserPort.currentUserId()).thenReturn(Mono.just(UUID.randomUUID()));
        when(currentUserPort.currentMunicipalCode()).thenReturn(Mono.just(UUID.randomUUID()));
        when(passwordEncoder.encode(anyString())).thenReturn("hashedpass");
        when(userPort.save(any(UserAccount.class))).thenReturn(Mono.just(testUser));
        when(externalAuthPort.createUser(anyString(), anyString(), anyString(), any())).thenReturn(Mono.just("keycloakId"));
        when(configServiceClientPort.getDefaultRolesByContext(any(), any(), any())).thenReturn(Flux.empty());

        StepVerifier.create(userService.createUser(command))
                .expectNext(testUser)
                .verifyComplete();

        verify(userPort, times(1)).save(any(UserAccount.class));
        verify(externalAuthPort).createUser(anyString(), anyString(), anyString(), any());
    }

    @Test
    void suspendUser_Success() {
        edu.pe.vallegrande.AuthenticationService.domain.model.user.SuspendUserCommand command =
            edu.pe.vallegrande.AuthenticationService.domain.model.user.SuspendUserCommand.builder()
                .reason("suspension reason")
                .suspensionEnd(java.time.LocalDateTime.now().plusDays(1))
                .build();

        when(userPort.findById(userId)).thenReturn(Mono.just(testUser));
        when(currentUserPort.currentUserId()).thenReturn(Mono.just(UUID.randomUUID()));
        when(userPort.save(any(UserAccount.class))).thenReturn(Mono.just(testUser));

        StepVerifier.create(userService.suspendUser(userId, command))
                .expectNext(testUser)
                .verifyComplete();

        verify(userPort).save(argThat(user -> "SUSPENDED".equals(user.getStatus())));
    }

    @Test
    void getUserById_Success() {
        when(userPort.findById(userId)).thenReturn(Mono.just(testUser));

        StepVerifier.create(userService.getUserById(userId))
                .expectNext(testUser)
                .verifyComplete();

        verify(userPort).findById(userId);
    }

    @Test
    void getUserById_NotFound() {
        when(userPort.findById(userId)).thenReturn(Mono.empty());

        StepVerifier.create(userService.getUserById(userId))
                .expectError(ResourceNotFoundException.class)
                .verify();

        verify(userPort).findById(userId);
    }

    @Test
    void restoreUser_Success() {
        UUID updatedBy = UUID.randomUUID();
        when(userPort.updateStatus(userId, "ACTIVE", updatedBy)).thenReturn(Mono.empty());
        when(userPort.findById(userId)).thenReturn(Mono.just(testUser));

        StepVerifier.create(userService.restoreUser(userId, updatedBy))
                .expectNext(testUser)
                .verifyComplete();

        verify(userPort).updateStatus(userId, "ACTIVE", updatedBy);
    }

    @Test
    void deleteUser_Success() {
        UUID updatedBy = UUID.randomUUID();
        when(userPort.updateStatus(userId, "INACTIVE", updatedBy)).thenReturn(Mono.empty());

        StepVerifier.create(userService.deleteUser(userId, updatedBy))
                .verifyComplete();

        verify(userPort).updateStatus(userId, "INACTIVE", updatedBy);
    }

    @Test
    void existsByUsername_True() {
        String username = "testuser";
        when(userPort.existsByUsername(username)).thenReturn(Mono.just(true));

        StepVerifier.create(userService.existsByUsername(username))
                .expectNext(true)
                .verifyComplete();
    }
}
