package edu.pe.vallegrande.AuthenticationService.scheduler;

import java.time.LocalDateTime;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.out.persistence.repository.UserRepository;
import edu.pe.vallegrande.AuthenticationService.infrastructure.util.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Scheduler para reactivación automática de usuarios */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "scheduler.user-unblock.enabled", havingValue = "true", matchIfMissing = true)
public class UserReactivationScheduler {
    
    private final UserRepository userRepository;
    
    /** Se ejecuta cada hora (cron: 0 0 * * * *) */
    @Scheduled(cron = "0 0 * * * *")
    public void reactivateSuspendedUsers() {
        log.info("Iniciando proceso de reactivación automática de usuarios suspendidos");
        
        LocalDateTime now = DateTimeUtil.nowInPeru();
        
        userRepository.findAll()
            .filter(user -> "SUSPENDED".equals(user.getStatus()))
            .filter(user -> user.getSuspensionEnd() != null)
            .filter(user -> DateTimeUtil.isPast(user.getSuspensionEnd()))
            .flatMap(user -> {
                log.info("Reactivando usuario suspendido: {} (suspendido hasta: {})", 
                    user.getUsername(), DateTimeUtil.formatForDisplay(user.getSuspensionEnd()));
                
                user.setStatus("ACTIVE");
                user.setSuspensionReason(null);
                user.setSuspensionStart(null);
                user.setSuspensionEnd(null);
                user.setSuspendedBy(null);
                user.setUpdatedAt(now);
                
                return userRepository.save(user);
            })
            .doOnNext(user -> log.info("Usuario reactivado: {}", user.getUsername()))
            .doOnError(error -> log.error("Error al reactivar usuario: {}", error.getMessage()))
            .subscribe();
        
        log.info("Proceso de reactivación completado");
    }
    
    /** Se ejecuta según configuración (por defecto cada minuto) para desbloquear usuarios automáticamente */
    @Scheduled(cron = "${scheduler.user-unblock.cron:0 * * * * *}")
    public void unblockUsers() {
        log.debug("Iniciando proceso de desbloqueo automático de usuarios");
        
        LocalDateTime now = DateTimeUtil.nowInPeru();
        
        // Usar la consulta optimizada que trae usuarios con bloqueo expirado (incluye los que expiran exactamente ahora)
        userRepository.findByBlockedUntilBefore(now)
            .flatMap(user -> {
                log.info("Desbloqueando usuario: {} (bloqueado hasta: {}, hora actual: {})", 
                    user.getUsername(), 
                    DateTimeUtil.formatForDisplay(user.getBlockedUntil()),
                    DateTimeUtil.formatForDisplay(now));
                
                // Solo modificar campos relacionados con el bloqueo
                user.setBlockedUntil(null);
                user.setBlockReason(null);
                user.setBlockStart(null);
                user.setLoginAttempts(0);
                user.setUpdatedAt(now);
                
                // Guardar el usuario completo
                return userRepository.save(user)
                    .doOnSuccess(savedUser -> log.info("Usuario {} desbloqueado exitosamente a las {}", 
                        savedUser.getUsername(), 
                        DateTimeUtil.formatForDisplay(now)));
            })
            .doOnError(error -> log.error("Error al desbloquear usuario: {}", error.getMessage()))
            .doOnComplete(() -> log.debug("Proceso de desbloqueo completado"))
            .subscribe();
    }
}
