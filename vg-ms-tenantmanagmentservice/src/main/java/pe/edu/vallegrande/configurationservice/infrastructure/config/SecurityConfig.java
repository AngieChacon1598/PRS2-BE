package pe.edu.vallegrande.configurationservice.infrastructure.config;

import com.nimbusds.jwt.JWTParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.ReactiveAuthenticationManagerResolver;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoders;
import org.springframework.security.oauth2.server.resource.authentication.JwtReactiveAuthenticationManager;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    private static final String MUNICIPALITIES_BASE_PATH = "/api/v1/municipalities";
    private static final String[] PUBLIC_GET_URLS = {
            MUNICIPALITIES_BASE_PATH,
            MUNICIPALITIES_BASE_PATH + "/**",
            MUNICIPALITIES_BASE_PATH + "/search/**",
            MUNICIPALITIES_BASE_PATH + "/validate/**"
    };

    private static final String[] PUBLIC_POST_URLS = {
            MUNICIPALITIES_BASE_PATH + "/register"
    };

    @Value("${spring.security.oauth2.resourceserver.jwt.keycloak.issuer-uri}")
    private String keycloakIssuer;

    @Value("${spring.security.oauth2.resourceserver.jwt.firebase.issuer-uri}")
    private String firebaseIssuer;

    @Value("${spring.security.oauth2.resourceserver.jwt.supabase.issuer-uri}")
    private String supabaseIssuer;

    private final CorsWebFilter corsWebFilter;

    public SecurityConfig(CorsWebFilter corsWebFilter) {
        this.corsWebFilter = corsWebFilter;
    }

    private static final String[] PUBLIC_URLS = {
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/webjars/**",
            "/actuator/health",
            "/actuator/info"
    };

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        

        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .addFilterAt(corsWebFilter, SecurityWebFiltersOrder.CORS)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(PUBLIC_URLS).permitAll()
                        .pathMatchers(HttpMethod.GET, PUBLIC_GET_URLS).permitAll()
                        .pathMatchers(HttpMethod.POST, PUBLIC_POST_URLS).permitAll()
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .anyExchange().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        // Intercept the token extraction. If the token isn't meant for this service
                        // (e.g., custom Auth service HS512 token), we pretend there is no token.
                        // This allows permitAll() endpoints to succeed completely anonymously.
                        .bearerTokenConverter(exchange -> {
                            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
                            if (authHeader != null && authHeader.toLowerCase().startsWith("bearer ")) {
                                String token = authHeader.substring(7);
                                try {
                                    String issuer = JWTParser.parse(token).getJWTClaimsSet().getIssuer();
                                    if (issuer == null || (!issuer.equals(keycloakIssuer) && !issuer.equals(firebaseIssuer) && !issuer.equals(supabaseIssuer))) {
                                        log.warn("Ignored token with unknown issuer: {}. Pretending anonymous.", issuer);
                                        return Mono.empty();
                                    }
                                } catch (Exception e) {
                                    log.warn("Ignored unparseable (HS512/custom) token. Pretending anonymous.");
                                    return Mono.empty();
                                }
                                return Mono.just(new BearerTokenAuthenticationToken(token));
                            }
                            return Mono.empty();
                        })
                        .authenticationManagerResolver(authenticationManagerResolver()));
        return http.build();
    }

    @Bean
    public ReactiveAuthenticationManagerResolver<ServerWebExchange> authenticationManagerResolver() {
        Map<String, ReactiveAuthenticationManager> managers = new HashMap<>();

        return exchange -> {
            String token = extractToken(exchange);
            if (token == null) {
                return Mono.empty();
            }
            try {
                String issuer = JWTParser.parse(token).getJWTClaimsSet().getIssuer();
                if (issuer == null) {
                    return Mono.just(authentication -> Mono.error(
                        new org.springframework.security.oauth2.core.OAuth2AuthenticationException("Invalid or missing issuer")
                    ));
                }
                return Mono.just(managers.computeIfAbsent(issuer, iss -> {
                    if (iss.equals(keycloakIssuer) || iss.equals(firebaseIssuer) || iss.equals(supabaseIssuer)) {
                        log.info("Initializing ReactiveAuthenticationManager for issuer: {}", iss);
                        return new JwtReactiveAuthenticationManager(ReactiveJwtDecoders.fromIssuerLocation(iss));
                    }
                    return authentication -> Mono.error(
                        new org.springframework.security.oauth2.core.OAuth2AuthenticationException("Unsupported issuer")
                    );
                }));
            } catch (Exception e) {
                return Mono.just(authentication -> Mono.error(
                    new org.springframework.security.oauth2.core.OAuth2AuthenticationException("Invalid token format")
                ));
            }
        };
    }

    private String extractToken(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}

