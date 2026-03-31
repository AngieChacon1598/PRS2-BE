package pe.edu.vallegrande.ms_maintenanceService.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import pe.edu.vallegrande.ms_maintenanceService.application.mapper.MaintenanceMapper;
import pe.edu.vallegrande.ms_maintenanceService.application.usecase.MaintenanceUseCase;
import pe.edu.vallegrande.ms_maintenanceService.domain.port.in.MaintenanceServicePort;
import pe.edu.vallegrande.ms_maintenanceService.domain.port.out.MaintenanceRepositoryPort;
import pe.edu.vallegrande.ms_maintenanceService.domain.port.out.ExternalTenantServicePort;
import pe.edu.vallegrande.ms_maintenanceService.domain.port.out.ExternalAssetServicePort;
import pe.edu.vallegrande.ms_maintenanceService.domain.service.MaintenanceValidator;

@Configuration
public class BeanConfig {

    @Bean
    public MaintenanceMapper maintenanceMapper() {
        return new MaintenanceMapper();
    }

    @Bean
    public MaintenanceValidator maintenanceValidator() {
        return new MaintenanceValidator();
    }

    @Bean
    public MaintenanceServicePort maintenanceServicePort(
            MaintenanceRepositoryPort repositoryPort,
            MaintenanceMapper mapper,
            MaintenanceValidator validator,
            ExternalTenantServicePort tenantService,
            ExternalAssetServicePort assetService) {
        return new MaintenanceUseCase(repositoryPort, mapper, validator, tenantService, assetService);
    }
}

