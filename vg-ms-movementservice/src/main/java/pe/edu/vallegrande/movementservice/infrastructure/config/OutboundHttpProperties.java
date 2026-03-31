package pe.edu.vallegrande.movementservice.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "services.http")
public class OutboundHttpProperties {

    private int connectTimeoutMs = 5_000;

    private int responseTimeoutSeconds = 10;

    private int userRequestTimeoutSeconds = 5;

    private int patrimonioRequestTimeoutSeconds = 10;

    private int retryTotalAttempts = 3;

    private long retryInitialIntervalMs = 200;

    private int retryMaxIntervalSeconds = 2;
}
