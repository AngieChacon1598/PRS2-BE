package edu.pe.vallegrande.AuthenticationService.scheduler;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.out.persistence.repository.UserRoleRepository;
import edu.pe.vallegrande.AuthenticationService.infrastructure.util.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Scheduler para desactivación automática de roles expirados
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "scheduler.role-expiration.enabled", havingValue = "true", matchIfMissing = true)
public class RoleExpirationScheduler {

    private final UserRoleRepository userRoleRepository;

    /** Se ejecuta diariamente a las 00:00 (configurable en application.yml) */
    @Scheduled(cron = "${scheduler.role-expiration.cron:0 0 0 * * *}")
    public void deactivateExpiredRoles() {
        log.info("Iniciando proceso de desactivación automática de roles expirados");

        userRoleRepository.findExpiredActiveRoles()
                .flatMap(userRole -> {
                    log.info("Desactivando rol expirado - Usuario: {}, Rol: {}, Fecha de expiración: {}",
                            userRole.getUserId(),
                            userRole.getRoleId(),
                            DateTimeUtil.formatForDisplay(userRole.getExpirationDate().atStartOfDay()));

                    userRole.setActive(false);

                    return userRoleRepository.save(userRole)
                            .doOnSuccess(savedRole -> log.info("Rol desactivado exitosamente - Usuario: {}, Rol: {}",
                                    savedRole.getUserId(), savedRole.getRoleId()));
                })
                .doOnError(error -> log.error("Error al desactivar rol expirado: {}", error.getMessage()))
                .subscribe();

        log.info("Proceso de desactivación de roles expirados completado");
    }
}
