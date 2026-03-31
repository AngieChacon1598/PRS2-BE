package edu.pe.vallegrande.AuthenticationService.scheduler;

import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.out.persistence.entity.UserRole;
import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.out.persistence.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleExpirationSchedulerTest {

    @Mock
    private UserRoleRepository userRoleRepository;

    @InjectMocks
    private RoleExpirationScheduler roleExpirationScheduler;

    private UserRole expiredRole;

    @BeforeEach
    void setUp() {
        expiredRole = new UserRole();
        expiredRole.setUserId(UUID.randomUUID());
        expiredRole.setRoleId(UUID.randomUUID());
        expiredRole.setExpirationDate(LocalDate.now().minusDays(1));
        expiredRole.setActive(true);
    }

    @Test
    void deactivateExpiredRoles_Success() {
        // Arrange
        when(userRoleRepository.findExpiredActiveRoles()).thenReturn(Flux.just(expiredRole));
        when(userRoleRepository.save(any(UserRole.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        // Act
        roleExpirationScheduler.deactivateExpiredRoles();

        // Assert
        verify(userRoleRepository, times(1)).findExpiredActiveRoles();
        verify(userRoleRepository, times(1)).save(argThat(role -> Boolean.FALSE.equals(role.getActive())));
    }

    @Test
    void deactivateExpiredRoles_Empty() {
        // Arrange
        when(userRoleRepository.findExpiredActiveRoles()).thenReturn(Flux.empty());

        // Act
        roleExpirationScheduler.deactivateExpiredRoles();

        // Assert
        verify(userRoleRepository, times(1)).findExpiredActiveRoles();
        verify(userRoleRepository, never()).save(any(UserRole.class));
    }

    @Test
    void deactivateExpiredRoles_Error() {
        // Arrange
        when(userRoleRepository.findExpiredActiveRoles()).thenReturn(Flux.error(new RuntimeException("DB Error")));

        // Act
        roleExpirationScheduler.deactivateExpiredRoles();

        // Assert
        verify(userRoleRepository, times(1)).findExpiredActiveRoles();
        verify(userRoleRepository, never()).save(any(UserRole.class));
    }
}
