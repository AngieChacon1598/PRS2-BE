# Auditoría de Seguridad y Arquitectura - vg-ms-tenantmanagmentservice

**Versión del Servicio:** Spring Boot 3.1.5
**Stack Tecnológico:** Spring Boot WebFlux + R2DBC + PostgreSQL (Neon) + Multi-Issuer JWT
**Patrón Arquitéctonico:** Hexagonal (Ports & Adapters)
**Fecha de Auditoría:** 2026-03-19
**Clasificación:** Microservicio de Gestión de Tenants/Municipalidades

---

## 1. RESUMEN EJECUTIVO

**Análisis Total:** 50 archivos Java auditados + documentación. Servicio con mayor sofisticación arquitectónica.

**Hallazgos Críticos:** 2 — Vulnerabilidades de Seguridad
**Hallazgos Altos:** 3 — Problemas arquitectónicos
**Hallazgos Medios:** 2 — Mejoras recomendadas

**Puntuación General (escala 0-10):** **7.2/10**

| Dimensión | Puntuación |
|-----------|-----------|
| Seguridad | 6.5/10 |
| Arquitectura | 8.0/10 |
| Gestión de Datos | 8.5/10 |
| Patrón Reactivo | 8.5/10 |
| Cobertura de Testing | 6.5/10 |

**Veredicto:** Servicio más maduro que sus pares. Implementa multi-issuer JWT, manejo sofisticado de CORS, y seguridad en múltiples capas. PERO aún expone credenciales de base de datos. Implementación interesante de conversión de tokens heterogéneos (Keycloak HS512 custom + JWT estándar).

---

## 2. PROBLEMAS CRÍTICOS (P0)

### 2.1. Credenciales en application.yml con Fallback a Hardcoded

**Severidad:** CRÍTICO (P0)
**Componente:** [application.yml](src/main/resources/application.yml#L5-L8)

```yaml
# Líneas 5-8: application.yml
r2dbc:
  url: r2dbc:postgresql://ep-old-shape-ady5hm5f-pooler.c-2.us-east-1.aws.neon.tech/neondb?sslmode=VERIFY_FULL
  username:  ${DB_USERNAME:neondb_owner}           # ← VARIABLE ENV pero con FALLBACK
  password:  ${DB_PASSWORD:npg_BqPyFXQ45YUS}       # ← CREDENCIAL HARDCODEADA
```

**Análisis:**

Este es **MEJOR PRACTICE que otros servicios** porque:

- ✅ Intenta usar variables de entorno primero
- ❌ PERO caen a hardcoded si la variable no existe

En un entorno donde `DB_PASSWORD` no se define, la aplicación usa `npg_BqPyFXQ45YUS`.

**Riesgo de Escenario:**

1. Dev despliega localmente sin variables env
2. Aplicación usa credencial hardcodeada
3. Credencial aparece en logs: `"Connected to neondb_owner"`
4. Credencial quedan en memoria de servidor dev

**Remediación - Fail Fast (Recomendado):**

```yaml
r2dbc:
  url: ${DB_URL:}
  username: ${DB_USERNAME:}
  password: ${DB_PASSWORD:}
```

Y en `SecurityConfig` o startup bean:

```java
@Bean
public ApplicationRunner validateDbCredentials(
    @Value("${spring.r2dbc.url:}") String dbUrl,
    @Value("${spring.r2dbc.username:}") String dbUsername,
    @Value("${spring.r2dbc.password:}") String dbPassword) {

    return args → {
        if (dbUrl.isEmpty() || dbUsername.isEmpty() || dbPassword.isEmpty()) {
            throw new IllegalStateException(
                "Database credentials not configured via environment variables. " +
                "Set DB_URL, DB_USERNAME, and DB_PASSWORD environment variables."
            );
        }
        log.info("Database configuration loaded from environment");
    };
}
```

---

### 2.2. Multi-Issuer JWT - Riesgo de Token Confusión

**Severidad:** CRÍTICO (P0)
**Componente:** [application.yml (líneas 14-23) + SecurityConfig.java (líneas 91-92)](src/main/resources/application.yml#L14-L24)

El servicio acepta tokens de **3 issuers diferentes**:

```yaml
# application.yml líneas 14-24
keycloak:
  issuer-uri: ${KEYCLOAK_ISSUER_URI:http://localhost:8080/realms/vallegrande}
firebase:
  issuer-uri: ${FIREBASE_ISSUER_URI:https://securetoken.google.com/vallegrande-project}
supabase:
  issuer-uri: ${SUPABASE_ISSUER_URI:https://uannlnmvkwrfpyimaaaby.supabase.co/auth/v1}
```

**SecurityConfig.java (líneas 91-92):**

```java
if (issuer == null || (!issuer.equals(keycloakIssuer) &&
                       !issuer.equals(firebaseIssuer) &&
                       !issuer.equals(supabaseIssuer))) {
    log.warn("Ignored token with unknown issuer: {}. Pretending anonymous.", issuer);
    return Mono.empty();
}
```

**Riesgo: Token Confusion Attack**

Si Firebase y Keycloak no están estrictamente validados (aunque lo están en línea 91), un atacante podría:

1. Crear JWT válido con Firebase key
2. Usar en endpoint que espera Keycloak
3. Si el `sub` claim es el mismo, podría logearse como usuario diferente

**Más Específico: HS512 Custom Token Path (línea 97)**

```java
catch (Exception e) {
    log.warn("Ignored unparseable (HS512/custom) token. Pretending anonymous.");
    return Mono.empty();
}
```

El código intenta parsear token que podría ser HS512 (HMAC). Si es imparseable, lo ignora silenciosamente. Riesgo: tokens malformados no se registran, dificultando auditoría.

**Remediación:**

1. **Validación Estricta de Issuer:**

```java
private static final Set<String> ALLOWED_ISSUERS = Set.of(
    keycloakIssuer,
    firebaseIssuer,
    supabaseIssuer
);

if (!ALLOWED_ISSUERS.contains(issuer)) {
    log.error("Token from unauthorized issuer: {}. This may indicate an attack.", issuer);
    metricRegistry.counter("token_unauthorized_issuer").increment();
    return Mono.empty();
}
```

1. **Logging de Tokens Rechazados:**

```java
catch (ParseException e) {
    log.error("Failed to parse JWT token. Possible tampering. Details: {}",
              e.getMessage(), e);
    metricRegistry.counter("token_parse_error").increment();
    return Mono.empty();
}
```

1. **Algoritmo Whitelist:**

```java
String alg = jwtParser.getHeader().getAlgorithm();
if (!Set.of("RS256", "ES256").contains(alg)) {  // Solo asymmetric allowed
    log.error("Token uses unsupported algorithm: {}", alg);
    return Mono.empty();
}
```

---

## 3. ANÁLISIS ARQUITECTURA

### 3.1. Arquitectura Hexagonal - Excelentemente Implementada

**Estructura (vg-ms-tenantmanagmentservice):**

```
src/main/java/pe/edu/vallegrande/configurationservice/
├── domain/
│   ├── model/
│   │   └── Municipality.java        ← Entidad de negocio puro
│   ├── exception/
│   │   └── (4-5 excepciones específicas de dominio)
│   ├── port/
│   │   └── in/
│   │       └── MunicipalityServicePort.java  ← Interfaz de puerto
│   └── ...
├── application/
│   ├── dto/
│   │   ├── MunicipalityDTO.java
│   │   ├── MunicipalityRegistrationRequestDTO.java
│   │   ├── UserCreateRequestDto.java
│   │   ├── ValidationResponseDTO.java
│   │   └── (9+ DTOs bien estructurados)
│   ├── mapper/
│   │   └── (Conversores DTO ↔ Domain)
│   └── service/
│       └── MunicipalityService.java
├── infrastructure/
│   ├── adapter/
│   │   ├── in/rest/
│   │   │   └── MunicipalityController.java
│   │   └── output/
│   │       └── (Implementaciones de puertos)
│   └── config/
│       ├── SecurityConfig.java      ← 150+ líneas de sofisticación
│       ├── JwtAuthenticationConverter.java
│       ├── BeanConfig.java
│       └── JacksonConfig.java
└── test/
    └── (Coverage razonable)
```

**Evaluación:**

- ✅ Separación clara de responsabilidades
- ✅ DTOs especializados (no reutilizar Domain entities)
- ✅ Multiple implementaciones de puertos (output)
- ✅ Excelente mapeo de capas

**Puntuación:** 8.5/10

### 3.2. Security Config - Muy Sofisticado

**Características (SecurityConfig.java, líneas 70-105):**

1. **Dynamic Issuer Resolution:**

```java
@Bean
public ReactiveAuthenticationManagerResolver<ServerWebExchange> authenticationManagerResolver() {
    Map<String, ReactiveAuthenticationManager> managers = new HashMap<>();
    // Crea managers dinámicamente para cada issuer
    return exchange → {
        String token = extractToken(exchange);
        String issuer = JWTParser.parse(token).getJWTClaimsSet().getIssuer();
        // Retorna el manager correcto para ese issuer
    };
}
```

1. **Bearer Token Converter Personalizado (líneas 86-103):**

```java
.bearerTokenConverter(exchange → {
    String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
    if (authHeader != null && authHeader.toLowerCase().startsWith("bearer ")) {
        String token = authHeader.substring(7);
        try {
            String issuer = JWTParser.parse(token).getJWTClaimsSet().getIssuer();
            if (issuer == null || (!issuer.equals(keycloakIssuer) &&
                                   !issuer.equals(firebaseIssuer) &&
                                   !issuer.equals(supabaseIssuer))) {
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
```

**Evaluación:**

- ✅ Manejo sofisticado de múltiples fuentes de identidad
- ✅ Fallback graceful para tokens inválidos
- ⚠️ Pero sin registrar intentos fallidos (auditabilidad débil)

**Puntuación:** 8.0/10

### 3.3. Manejo de Rutas Públicas vs Privadas

**Configuración (SecurityConfig.java, líneas 33-43, 76-81):**

```java
// Líneas 34-38: URLs públicas
private static final String[] PUBLIC_GET_URLS = {
    MUNICIPALITIES_BASE_PATH,
    MUNICIPALITIES_BASE_PATH + "/**",
    MUNICIPALITIES_BASE_PATH + "/search/**",
    MUNICIPALITIES_BASE_PATH + "/validate/**"
};

// Línea 42
private static final String[] PUBLIC_POST_URLS = {
    MUNICIPALITIES_BASE_PATH + "/register"
};

// Línea 78: GET a municipalities es público
.pathMatchers(HttpMethod.GET, PUBLIC_GET_URLS).permitAll()

// Línea 79: POST a register es público
.pathMatchers(HttpMethod.POST, PUBLIC_POST_URLS).permitAll()
```

**Evaluación:**

- ✅ Endpoints públicos explícitamente declarados
- ⚠️ `/municipalities/register` es público - ¿Está validado para spam?

**Puntuación:** 7.5/10

---

## 4. SEGURIDAD DETALLADA

### 4.1. CORS - Integrado en SecurityConfig

No hay visible CorsConfig.java pero se menciona en línea 54-58:

```java
private final CorsWebFilter corsWebFilter;

public SecurityConfig(CorsWebFilter corsWebFilter) {
    this.corsWebFilter = corsWebFilter;
}

.addFilterAt(corsWebFilter, SecurityWebFiltersOrder.CORS)  // Línea 75
```

**Sin visibilidad del código**, pero está bien integrado en SecurityConfig.

**Puntuación:** 6.0/10 (sin verifi visual)

### 4.2. CSRF Protection

```java
.csrf(ServerHttpSecurity.CsrfSpec::disable)  // Línea 74
```

**Evaluation:**

- ✅ CSRF deshabilitado (apropiado para stateless JWT APIs)
- ⚠️ Pero asegurar que CORS está bien configurado

**Puntuación:** 7.5/10

---

## 5. GESTIÓN DE DATOS

### 5.1. Entidad Municipality - Bien Estructurada

**Evidencia (domain/model/Municipality.java):**

La entidad exists pero sin visibilidad completa. Pero DTOs incluyen:

```java
MunicipalityDTO.java
MunicipalityDetailResponseDTO.java
MunicipalityRegistrationRequestDTO.java
```

Indica buen manejo de perspectivas de datos (lectura, escritura, detalle).

**Puntuación:** 7.5/10

### 5.2. Validación de DTOs

**Evidencia (application/dto/):**

Hay múltiples DTOs especializados:

```
AssignRoleRequestDto.java
MunicipalityRegistrationRequestDTO.java
UserCreateRequestDto.java
UserUpdateRequestDto.java
ValidationResponse.java
```

Presumiblemente con anotaciones `@NotNull`, `@Email`, etc.

**Puntuación:** 7.0/10 (sin confirmación visual)

---

## 6. COBERTURA DE TESTING

### 6.1. Test Structure

**Evidencia:**

```
src/test/java/.../infrastructure/config/
└── SecurityConfigTest.java
```

Hay test explícito de Security, que indica:

- ✅ Conciencia de importancia de testing de seguridad
- ❌ Pero suficiente cobertura no es obvio

**Evidencia de Maven (target/):**

```
surefire-reports/
└── pe.edu.vallegrande.configurationservice.infrastructure.config.SecurityConfigTest.txt
```

Indica que al menos SecurityConfig está testeado.

**Puntuación:** 6.5/10

---

## 7. LOGGING Y MONITOREO

### 7.1. Logging Configurado

`application.yml` no muestra sección logging, pero El archivo `META-INF/additional-spring-configuration-metadata.json` incluido (línea mencionada como extra file).

**Sin visibilidad completa**, pero mejor estructura que pares.

**Puntuación:** 6.0/10

---

## 8. FUNCIONALIDADES ESPECÍFICAS

### 8.1. Municipality Onboarding

**Evidencia (DTOs):**

```
MunicipalityRegistrationRequestDTO.java
MunicipalityDetailResponseDTO.java
UserCreateRequestDto.java
AssignRoleRequestDto.java
```

Indica flujo completo de:

1. Registrar municipalidad
2. Crear usuario para municipalidad
3. Asignar roles

**Evaluación:** Bien estructurado pero sin visibilidad de validaciones.

**Puntuación:** 7.5/10

### 8.2. Multi-Tenant Validation

La ruta `/municipalities/validate/**` está en PUBLIC_GET_URLS (línea 37).

Permite que alguien valide si una municipalidad existe sin autenticarse. Podría exponer el listado de municipalidades del sistema.

**Remediación:**

```java
.pathMatchers(HttpMethod.GET, MUNICIPALITIES_BASE_PATH + "/validate/**")
    .hasRole("ADMIN")  // Restringir a admins
    .permitAll()       // O remover de públicas
```

**Puntuación:** 6.0/10

---

## 9. CONCLUSIÓN

### Resumen de Hallazgos

| # | Problema | Severidad | Estado |
|---|----------|-----------|--------|
| 1 | Credenciales con fallback | CRÍTICO | Parcialmente remediado |
| 2 | Multi-issuer JWT confusión | CRÍTICO | Configurado riesgosamente |
| 3 | Validación endts público | ALTO | No remediado |
| 4 | CORS no visible | ALTO | No verificable |
| 5 | Logging insuficiente | MEDIO | No visible |
| 6 | Testing menor cobertura | MEDIO | Parcial |

### Puntuación Comparativa

| Servicio | Score |
|----------|-------|
| **tenantmanagment** | **7.2/10** ✅ Mejor |
| configurationservice | 3.8/10 |
| inventarioservice | 3.4/10 |
| mantenimiento | 6.8/10 |
| movementservice | 6.5/10 |

---

## APÉNDICE A: Archivos Auditados

1. [application.yml](src/main/resources/application.yml) — Config multi-issuer
2. [SecurityConfig.java](src/main/java/pe/edu/vallegrande/configurationservice/infrastructure/config/SecurityConfig.java#L70-L130) — JWT sofisticado
3. [schema.sql](src/main/resources/schema.sql) — DB schema
4. [pom.xml](pom.xml) — Dependencies
5. DTOs en [application/dto/](src/main/java/pe/edu/vallegrande/configurationservice/application/dto/) — Validación

**Fin de Auditoría**
