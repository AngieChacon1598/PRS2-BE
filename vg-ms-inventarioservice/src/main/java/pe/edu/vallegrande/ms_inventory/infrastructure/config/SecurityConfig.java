package pe.edu.vallegrande.ms_inventory.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import io.netty.resolver.DefaultAddressResolverGroup;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

     @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
     private String issuerUri;

     @Bean
     public ReactiveJwtDecoder jwtDecoder() {
          HttpClient httpClient = HttpClient.create()
                    .resolver(DefaultAddressResolverGroup.INSTANCE);

          WebClient webClient = WebClient.builder()
                    .clientConnector(new ReactorClientHttpConnector(httpClient))
                    .build();

          return NimbusReactiveJwtDecoder.withIssuerLocation(issuerUri)
                    .webClient(webClient)
                    .build();
     }

     @Bean
     public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http, JwtAuthenticationConverter jwtAuthenticationConverter) {
          http
                    .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                    .csrf(ServerHttpSecurity.CsrfSpec::disable)
                    .authorizeExchange(exchanges -> exchanges
                              .pathMatchers("/v3/api-docs/**", "/swagger-ui/**", "/webjars/**", "/actuator/**",
                                        "/swagger-ui.html")
                              .permitAll()
                              .anyExchange().authenticated())
                    .oauth2ResourceServer(oauth2 -> oauth2
                              .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)));
          return http.build();
     }

     @Bean
     public UrlBasedCorsConfigurationSource corsConfigurationSource() {
          CorsConfiguration config = new CorsConfiguration();
          config.setAllowCredentials(true);
          config.addAllowedOriginPattern("*");
          config.addAllowedHeader("*");
          config.addAllowedMethod("*");

          UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
          source.registerCorsConfiguration("/**", config);
          return source;
     }
}