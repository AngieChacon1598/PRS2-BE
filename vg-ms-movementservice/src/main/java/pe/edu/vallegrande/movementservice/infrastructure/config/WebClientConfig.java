package pe.edu.vallegrande.movementservice.infrastructure.config;

import io.netty.channel.ChannelOption;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(OutboundHttpProperties.class)
public class WebClientConfig {

    @Bean
    public WebClient.Builder webClientBuilder(OutboundHttpProperties http) {
        HttpClient reactorHttpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, http.getConnectTimeoutMs())
                .responseTimeout(Duration.ofSeconds(http.getResponseTimeoutSeconds()));
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(reactorHttpClient));
    }
}
