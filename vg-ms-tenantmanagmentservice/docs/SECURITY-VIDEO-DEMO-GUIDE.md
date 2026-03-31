# 🎥 Guía Completa: Video Demostrativo de Securización de Microservicios

## Descripción General
Este documento sirve como **guía de producción** para grabar un video demostrativo que explique cómo el microservicio **vg-ms-tenantmanagmentservice** implementa seguridad usando **JWT**, **Keycloak** y **Spring Security**.

**Duración sugerida del video**: 12-15 minutos  
**Audiencia**: Equipo técnico, revisores, stakeholders  
**Requisitos técnicos**: 
- OBS Studio (para grabar pantalla)
- Postman o Insomnia (para probar endpoints)
- IDE (VS Code o IntelliJ IDEA)
- Keycloak ejecutándose localmente o acceso a instancia remota

---

## 📋 Estructura del Video (Guión)

### **Sección 1: Introducción (1-2 min)**

**Contenido a mostrar en pantalla:**
- Nombre del proyecto: `vg-ms-tenantmanagmentservice`
- Objetivo: Explicar la arquitectura de seguridad implementada

**Script:**
```
"Hola, hoy vamos a mostrar cómo hemos securizado nuestro microservicio 
de Tenant Management usando tecnologías de estándar industrial como 
JWT, Keycloak y Spring Security.

Este microservicio es responsable de la gestión de municipalidades y 
requiere autenticación y autorización para proteger operaciones críticas.

En este video veremos:
1. La arquitectura de seguridad implementada
2. Cómo funciona el flujo de autenticación
3. Demostración práctica con Postman
4. Explicación del código fuente
5. Alternativas de mejora para el equipo"
```

---

### **Sección 2: Arquitectura de Seguridad (2-3 min)**

**Contenido a mostrar en pantalla:**
- Diagrama de flujo de autenticación (Mermaid o PowerPoint)
- Estructura de carpetas del proyecto

**Script:**
```
"Nuestra arquitectura se basa en el patrón OAuth2 Resource Server. 
Esto significa que nuestro microservicio NO almacena usuarios ni contraseñas.
En su lugar, actúa como un SERVIDOR DE RECURSOS que valida tokens JWT 
emitidos por servidores de identidad externos.

Los componentes clave son:

1. **Los Emisores de Tokens** (Identity Providers):
   - Keycloak: Sistema de gestión de identidades de código abierto
   - Firebase: Servicio de Google
   - Supabase: Alternativa a Firebase con PostgreSQL

2. **El Microservicio**: Valida que los tokens sean legítimos sin 
   necesidad de almacenar secretos de usuarios.

3. **Los Clientes**: Frontend, Postman, sistemas internos que obtienen 
   un token y lo envían con cada petición.

Esta arquitectura tiene ventajas clave:
✅ Escalabilidad: No necesita almacenar sesiones
✅ Seguridad: Los tokens se validan criptográficamente
✅ Flexibilidad: Soporta múltiples emisores simultáneamente
✅ Mantenimiento: La gestión de usuarios está centralizada en Keycloak"
```

**Mostrar en IDE:**
- Abrir la carpeta `/infrastructure/config/` y resaltar `SecurityConfig.java`

---

### **Sección 3: Flujo de Autenticación Detallado (2-3 min)**

**Contenido a mostrar:**
- Diagrama de secuencia del flujo

**Diagrama (copiable para el video):**
```
┌─────────────┐
│   Cliente   │
│ (Frontend)  │
└──────┬──────┘
       │
       │ 1. GET /login (usuario/contraseña)
       ▼
┌──────────────────┐
│    Keycloak      │ ← Valida credenciales
│   (IdP)          │
└────────┬─────────┘
         │
         │ 2. Retorna JWT Token
         │
       ▼
┌──────────────────────────────────────┐
│ Token JWT: eyJhbGc... (largo base64) │
│ Estructura:                          │
│ - Header: { alg: RS256, kid: ... }   │
│ - Payload: { sub, iss, roles, ... }  │
│ - Signature: (firmado con clave)     │
└────────┬─────────────────────────────┘
         │
         │ 3. Authorization: Bearer <TOKEN>
         ▼
┌──────────────────────────────────────┐
│  Microservicio (Resource Server)     │
│  vg-ms-tenantmanagmentservice        │
└────────┬─────────────────────────────┘
         │
         ├→ 4a. Extrae el token del header
         │
         ├→ 4b. Lee el issuer (iss) del token
         │
         ├→ 4c. Descarga claves públicas de Keycloak
         │
         ├→ 4d. Valida la firma del token
         │
         ├→ 4e. Verifica roles y permisos
         │
         └→ 4f. Evalúa @PreAuthorize("hasRole('SUPER_ADMIN')")
         │
       ▼
      ✅ Token válido → Permite acceso al endpoint
      ❌ Token inválido/expirado → Retorna 401 Unauthorized
```

**Script:**
```
"El flujo es así:

Primero, el usuario se autentica en Keycloak proporcionando 
su usuario y contraseña. Keycloak valida y genera un TOKEN JWT.

Un JWT tiene tres partes separadas por puntos:

HEADER: Información sobre el algoritmo (RS256 = RSA con SHA-256)

PAYLOAD: Los datos del usuario en formato JSON. Aquí van:
- 'sub': el ID único del usuario
- 'iss': el issuer (quién emitió el token, ej: Keycloak)
- 'roles': los roles que tiene el usuario
- 'exp': cuándo expira el token
- otros claims personalizados

SIGNATURE: Una firma criptográfica que prueba que Keycloak 
es el que emitió el token y que no fue modificado.

Cuando el cliente envía una petición a nuestro microservicio 
con el token en el header 'Authorization: Bearer <TOKEN>':

1. Nuestro código SecurityConfig.java lo intercepta
2. Extrae el token del header
3. Lee el campo 'issuer' para saber quién lo emitió
4. Descarga las claves públicas del emisor (Keycloak)
5. Valida que la firma sea válida
6. Si todo está bien, extrae los roles
7. Verifica si el usuario tiene el rol requerido para ese endpoint
8. Si sí → permite el acceso
   Si no → retorna error 403 Forbidden"
```

---

### **Sección 4: Demostración Práctica en Postman (4-5 min)**

#### **4.1 Paso 1: Obtener un Token JWT desde Keycloak**

**En Postman:**

1. Crear nueva petición POST
2. URL: `http://localhost:8080/realms/vallegrande/protocol/openid-connect/token`
3. Body (form-data):
   ```
   client_id: vg-tenant-client
   username: admin@vallegrande.edu.pe
   password: admin123
   grant_type: password
   ```
4. Mostrar la respuesta con el `access_token`

**Script:**
```
"Primero, obtenemos un token. Vamos a usar Keycloak localmente.

En Postman hacemos una petición POST al endpoint de tokens de Keycloak,
pasando nuestras credenciales. Keycloak valida y nos retorna un JSON 
con el access_token.

Este token es un JWT que podemos copiar y usar en las siguientes peticiones."
```

#### **4.2 Paso 2: Decodificar el JWT (jwt.io)**

**En el navegador:**
1. Ir a https://jwt.io
2. Pegar el token en el área izquierda
3. Mostrar el Header, Payload y Signature colorido

**Resaltar en el Payload:**
```json
{
  "sub": "8e8a4c15-fdbf-4e18-b74d-1234567890ab",
  "iss": "http://localhost:8080/realms/vallegrande",
  "aud": "account",
  "typ": "Bearer",
  "azp": "vg-tenant-client",
  "exp": 1710764400,
  "iat": 1710764100,
  "realm_access": {
    "roles": ["SUPER_ADMIN", "ONBOARDING_MANAGER"]
  },
  "resource_access": {
    "vg-tenant-client": {
      "roles": ["TENANT_ADMIN"]
    }
  }
}
```

**Script:**
```
"Aquí podemos ver el contenido del token. No está encriptado, 
solo está codificado en base64. Cualquiera puede leer qué dice.

Lo importante es la SIGNATURE (el tercera parte), que es lo que 
impide que alguien modifique el contenido sin que se note.

Vemos que contiene:
- El issuer: 'http://localhost:8080/realms/vallegrande' 
  (es nuestro Keycloak local)
- Los roles: 'SUPER_ADMIN', 'ONBOARDING_MANAGER'
- La fecha de expiración (exp)

Si alguien intenta cambiar los roles sin la clave privada de Keycloak,
la signature no coincidirá y nuestro microservicio lo rechazará."
```

#### **4.3 Paso 3: Llamar a Endpoint PROTEGIDO con Token**

**En Postman:**

**Petición 1: GET /api/v1/municipalities (CON TOKEN - DEBE FUNCIONAR)**
```
GET http://localhost:5001/api/v1/municipalities
Headers:
  Authorization: Bearer eyJhbGc...
```

**Script:**
```
"Ahora usamos el token. Hacemos una petición GET a /api/v1/municipalities.
Este endpoint requiere el rol 'SUPER_ADMIN' o 'ONBOARDING_MANAGER'.
Como nuestro token tiene 'SUPER_ADMIN', debería funcionar."
```

**Mostrar resultado:**
```json
[
  {
    "id": "550e8400-e29b-41d4-a716",
    "name": "Municipalidad de Lima",
    "type": "METROPOLITAN",
    "department": "Lima",
    ...
  },
  ...
]
```

**Petición 2: POST /api/v1/municipalities (SIN TOKEN - DEBE FALLAR)**
```
POST http://localhost:5001/api/v1/municipalities
Headers:
  (Sin Authorization header)
Body:
{
  "name": "Nueva Municipalidad",
  ...
}
```

**Script:**
```
"Ahora intentamos crear una municipalidad SIN enviar un token.
Este endpoint requiere autenticación, así que debería retornar 
error 401 Unauthorized."
```

**Mostrar error:**
```json
{
  "error": "unauthorized",
  "error_description": "Full authentication is required to access this resource"
}
```

**Petición 3: POST /api/v1/municipalities (CON TOKEN VÁLIDO)**
```
POST http://localhost:5001/api/v1/municipalities
Headers:
  Authorization: Bearer eyJhbGc...
Body:
{
  "name": "Nueva Municipalidad",
  "type": "PROVINCIAL",
  "department": "Cusco",
  ...
}
```

**Script:**
```
"Ahora con el token, la misma operación funciona.
El servidor valida el token, verifica que tiene el rol 'SUPER_ADMIN'
(que es lo que requiere este endpoint), y permite la creación."
```

**Mostrar resultado:**
```json
{
  "id": "660e8400-e29b-41d4-b827",
  "name": "Nueva Municipalidad",
  ...
}
```

---

### **Sección 5: Explicación del Código Fuente (3-4 min)**

#### **5.1 Archivo: SecurityConfig.java**

**Abrir en IDE y navegar por secciones:**

**PARTE 1: Imports y anotaciones clave**
```java
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {
```

**Script:**
```
"El archivo SecurityConfig.java es el CORAZÓN de nuestra seguridad.

@EnableWebFluxSecurity: Activa el módulo de seguridad para WebFlux
                       (framework reactivo de Spring)

@EnableReactiveMethodSecurity: Habilita @PreAuthorize en métodos"
```

**PARTE 2: Definición de URLs públicas**
```java
private static final String[] PUBLIC_URLS = {
    "/swagger-ui.html",
    "/swagger-ui/**",
    "/v3/api-docs/**",
    "/actuator/health",
    "/actuator/info"
};

private static final String[] PUBLIC_GET_URLS = {
    MUNICIPALITIES_BASE_PATH,
    MUNICIPALITIES_BASE_PATH + "/**",
    ...
};

private static final String[] PUBLIC_POST_URLS = {
    MUNICIPALITIES_BASE_PATH + "/register"
};
```

**Script:**
```
"Aquí definimos qué rutas son públicas (sin autenticación requerida):

- Swagger y documentación: Para que el equipo técnico pueda 
  ver los endpoints disponibles
- Actuator: Para monitoreo y health checks
- GET de municipalidades: Permite consultar el catálogo sin token
- POST /register: Permite el registro inicial de municipalidades

El resto de operaciones (POST crear, PUT actualizar, DELETE) 
requieren autenticación."
```

**PARTE 3: Configuración de los Emisores**
```java
@Value("${spring.security.oauth2.resourceserver.jwt.keycloak.issuer-uri}")
private String keycloakIssuer;

@Value("${spring.security.oauth2.resourceserver.jwt.firebase.issuer-uri}")
private String firebaseIssuer;

@Value("${spring.security.oauth2.resourceserver.jwt.supabase.issuer-uri}")
private String supabaseIssuer;
```

**Script:**
```
"Aquí inyectamos las URIs de los servidores de identidad 
desde el archivo application.yml. 

Esto es importante porque permite que cambiar de servidor 
de identidad sea solo cambiar una variable de ambiente, 
sin necesidad de recompilar."
```

**PARTE 4: Chain de Seguridad**
```java
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
            .bearerTokenConverter(...)
            .authenticationManagerResolver(authenticationManagerResolver()));
    return http.build();
}
```

**Script:**
```
"Este es el filtro principal que intercepta TODAS las peticiones.

.csrf().disable(): Deshabilitamos CSRF porque esta es una API REST 
                   (no es apropiado para APIs sin sesión)

.authorizeExchange(): Define las reglas de autorización:
- PUBLIC_URLS: Acceso libre
- GET público: Acceso libre (solo lectura del catálogo)
- POST /register: Acceso libre (onboarding inicial)
- OPTIONS: Acceso libre (para CORS preflight)
- todo lo demás: Requiere autenticación (.authenticated())

.oauth2ResourceServer(): Configura la validación de JWT
- bearerTokenConverter: Extrae el token del header Authorization
- authenticationManagerResolver: Determina cuál emisor usamos"
```

**PARTE 5: Resolución Dinámica de Emisores**
```java
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
            return Mono.just(managers.computeIfAbsent(issuer, iss -> {
                if (iss.equals(keycloakIssuer) || iss.equals(firebaseIssuer) || iss.equals(supabaseIssuer)) {
                    return new JwtReactiveAuthenticationManager(
                        ReactiveJwtDecoders.fromIssuerLocation(iss)
                    );
                }
                return authentication -> Mono.error(...);
            }));
        }
        ...
    };
}
```

**Script:**
```
"Aquí está la magia: la resolución DINÁMICA de emisores.

El código:
1. Extrae el token de la petición
2. Lee el campo 'issuer' (iss) del payload del JWT
3. Comprueba si es uno de nuestros emisores autorizados:
   - Keycloak
   - Firebase
   - Supabase
4. Si es válido, descarga las claves públicas de ese emisor
5. Crea un AuthenticationManager específico para ese emisor
6. Valida el token usando esas claves

¿Por qué es importante esto? Porque permite que en el futuro
podamos aceptar tokens de múltiples servicios de identidad 
sin necesidad de cambiar la lógica del microservicio.

También implementa CARGA PEREZOSA: solo descarga las claves 
cuando recibe el primer token de un emisor determinado.
Esto evita que el servidor falle si Keycloak está caído al arrancar."
```

---

#### **5.2 Archivo: application.yml**

**Mostrar en IDE:**
```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          keycloak:
            issuer-uri: http://localhost:8080/realms/vallegrande
          firebase:
            issuer-uri: https://securetoken.google.com/vallegrande-project
          supabase:
            issuer-uri: https://uannlnmvkwrfpyimaaby.supabase.co/auth/v1
```

**Script:**
```
"En el archivo de configuración definimos las URLs de los 
servidores de identidad que soportamos.

Esto está externalizados en variables de ambiente para que 
en producción podamos usar URLs diferentes sin cambiar el código."
```

---

#### **5.3 Archivo: MunicipalityController.java**

**Mostrar decoradores en métodos:**
```java
@GetMapping
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ONBOARDING_MANAGER')")
public Flux<MunicipalityDTO> findAll() { ... }

@PostMapping
@ResponseStatus(HttpStatus.CREATED)
@PreAuthorize("hasRole('SUPER_ADMIN')")
public Mono<MunicipalityDTO> create(@RequestBody Municipality municipality) { ... }

@DeleteMapping("/{id}")
@ResponseStatus(HttpStatus.NO_CONTENT)
@PreAuthorize("hasRole('SUPER_ADMIN')")
public Mono<Void> delete(@PathVariable UUID id) { ... }
```

**Script:**
```
"En los controladores usamos @PreAuthorize para declarar 
qué roles se necesitan en cada endpoint.

Estos decoradores son evaluados por el módulo 
@EnableReactiveMethodSecurity que habilitamos al principio.

Ejemplos:

GET /municipalities: Requiere 'SUPER_ADMIN' O 'ONBOARDING_MANAGER'
POST /municipalities: Requiere 'SUPER_ADMIN' solamente
DELETE /municipalities/{id}: Requiere 'SUPER_ADMIN' solamente

Si un usuario con rol 'TENANT_ADMIN' intenta hacer DELETE,
Spring Security rechazan la petición automáticamente con error 403."
```

---

#### **5.4 pom.xml - Dependencias de Seguridad**

**Mostrar sección de dependencias:**
```xml
<!-- Spring Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- OAuth2 Resource Server -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>

<!-- JWT Parsing (Nimbus) -->
<dependency>
    <groupId>com.nimbusds</groupId>
    <artifactId>nimbus-jose-jwt</artifactId>
    <version>9.31</version>
</dependency>
```

**Script:**
```
"Las librerías que hacen todo esto posible son:

spring-boot-starter-security: El módulo base de Spring Security
spring-boot-starter-oauth2-resource-server: Soporte para validar JWTs
nimbus-jose-jwt: Una librería especializada en parsear y validar JWTs

Estas dependencias están en Maven, así que se descargan automáticamente
cuando compilamos el proyecto."
```

---

### **Sección 6: Ventajas y Características Implementadas (2 min)**

**Slide/Pantalla:**

```
✅ SEGURIDAD IMPLEMENTADA

1. AUTENTICACIÓN BASADA EN TOKENS
   - Los usuarios se autentican en Keycloak
   - Keycloak emite JWTs con información del usuario
   - El microservicio valida estos tokens

2. AUTORIZACIÓN POR ROLES
   - Cada endpoint declara qué roles necesita
   - Múltiples roles soportados
   - Validación centralizada

3. MULTI-EMISOR (MULTI-TENANT IDENTITY)
   - Soporta Keycloak, Firebase, Supabase
   - Resolución dinámica por issuer
   - Escalable a nuevos emisores

4. VALIDACIÓN CRIPTOGRÁFICA
   - Tokens firmados con RSA (RS256)
   - Imposible falsificar sin clave privada
   - Verificación de cada petición

5. APIs SIN ESTADO (STATELESS)
   - No requiere sesiones en el servidor
   - Escalable horizontalmente
   - Fácil de distribuir en múltiples instancias

6. SEGURIDAD REACTIVA
   - Implementado con WebFlux
   - Manejo no-bloqueante de I/O
   - Mejor rendimiento bajo carga

7. CORS PERSONALIZADO
   - Control de origen de peticiones
   - Protección contra ataques CSRF
```

**Script:**
```
"Con esta implementación hemos logrado una arquitectura 
de seguridad de nivel empresarial.

Los usuarios están protegidos porque:
- Sus credenciales nunca viajan a través del microservicio
- Cada petición es validada criptográficamente
- Los permisos se validan en cada operación
- La información del usuario está centralizada en Keycloak

El equipo de desarrollo se beneficia porque:
- La seguridad está centralizada en un lugar
- Agregar nuevos endpoints es tan simple como añadir @PreAuthorize
- Cambiar de proveedor de identidad es configurable, no de código
- El código es legible y mantenible"
```

---

### **Sección 7: Alternativas de Mejora (2-3 min)**

**Mostrar en pantalla/slides:**

#### **Mejora 1: Enriquecimiento de Claims Personalizados**

```java
// Versión actual: solo valida firma
.oauth2ResourceServer(oauth2 -> oauth2
    .bearerTokenConverter(...)
    .authenticationManagerResolver(...))

// Versión mejorada: mapea claims a authorities
@Bean
public JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
    JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
    authoritiesConverter.setAuthoritiesClaimName("realm_access.roles");
    authoritiesConverter.setAuthorityPrefix("ROLE_");
    converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
    return converter;
}
```

**Script:**
```
"Actualmente, extraemos los roles del token y los validamos.

Una mejora sería crear un JwtAuthenticationConverter 
que mapee claims personalizados del token a authorities de Spring.

Esto permitiría:
- Validaciones más granulares
- Soporte para scopes y permisos específicos
- Integración más profunda con Spring Security"
```

---

#### **Mejora 2: Auditoría de Intentos de Acceso**

```java
@Slf4j
@Component
public class SecurityAuditFilter implements WebFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return chain.filter(exchange)
            .doFinally(signalType -> {
                HttpRequest request = exchange.getRequest();
                HttpStatus status = exchange.getResponse().getStatusCode();
                
                if (status.is4xxClientError() || status.is5xxServerError()) {
                    log.warn("SECURITY: {} {} - Status: {}", 
                        request.getMethod(), 
                        request.getPath(), 
                        status);
                }
            });
    }
}
```

**Script:**
```
"Podríamos agregar un WebFilter que registre todos los intentos 
de acceso fallidos (401, 403, etc).

Esto permitiría:
- Detectar intentos de acceso no autorizados
- Identificar patrones de ataque
- Generar alertas automáticas
- Cumplir requisitos de auditoría"
```

---

#### **Mejora 3: Rate Limiting para Prevenir Fuerza Bruta**

```xml
<!-- pom.xml -->
<dependency>
    <groupId>io.github.bucket4j</groupId>
    <artifactId>bucket4j-core</artifactId>
    <version>7.6.0</version>
</dependency>
```

```java
@Bean
public RateLimitingFilter rateLimitingFilter() {
    return new RateLimitingFilter()
        .limitRequestsPerMinute(100, "/api/v1/municipalities")
        .limitRequestsPerMinute(10, "/api/v1/auth/login");
}
```

**Script:**
```
"Para prevenir ataques de fuerza bruta, podemos implementar 
rate limiting usando la librería bucket4j.

Esto permitiría:
- Limitar peticiones por IP o usuario
- Proteger endpoints sensibles (login)
- Responder automáticamente a patrones de ataque
- Escalar bajo carga maliciosa"
```

---

#### **Mejora 4: Validación de Tenant Isolation**

```java
@Aspect
@Component
public class TenantIsolationAspect {
    
    @Before("@PreAuthorize('hasRole(TENANT_ADMIN)')")
    public void validateTenantAccess(JoinPoint joinPoint) {
        String tokenTenant = getCurrentTenantFromToken();
        UUID resourceTenant = extractTenantFromResource(joinPoint);
        
        if (!tokenTenant.equals(resourceTenant)) {
            throw new TenantAccessDeniedException(
                "Acceso denegado: intento de acceder a tenant no autorizado"
            );
        }
    }
}
```

**Script:**
```
"Actualmente soportamos roles globales (SUPER_ADMIN) y por tenant (TENANT_ADMIN).

Una mejora sería asegurar que usuarios TENANT_ADMIN solo accedan 
a sus propios datos, no a los de otros tenants.

Esto se lograría con un Aspect que valide antes de cada operación:
- Extrae el tenant del token
- Extrae el tenant del recurso solicitado
- Compara que sean iguales
- Si no, rechaza la operación"
```

---

#### **Mejora 5: HTTPS Enforcement en Producción**

```yaml
# application-prod.yml
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-type: PKCS12
    key-store-password: ${SSL_KEYSTORE_PASSWORD}
    key-alias: tomcat

spring:
  security:
    require-https: true
```

**Script:**
```
"En producción, todo debe viajar sobre HTTPS.

Esto requiere:
- Un certificado SSL válido (Let's Encrypt es gratuito)
- Configurar el servidor para usar el certificado
- Forzar redirección de HTTP a HTTPS
- Usar HSTS headers para indicar que solo se acepta HTTPS"
```

---

#### **Mejora 6: API Key como Fallback Alternativo**

```java
// SecurityConfig.java - Agregar soporte para API Keys
.http(http -> http
    .addFilterBefore(apiKeyAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
    .oauth2ResourceServer(...))
```

**Script:**
```
"Para servicios internos o integraciones específicas, 
podríamos soportar API Keys como alternativa a JWT.

Esto es útil para:
- Cron jobs internos
- Integraciones con sistemas legacy
- Testing automatizado
- Acceso de máquina a máquina (M2M)"
```

---

### **Sección 8: Demo Final y Q&A (1-2 min)**

**Script:**
```
"Recapitulando, hemos mostrado:

1. ✅ Arquitectura: OAuth2 Resource Server con JWT
2. ✅ Implementación: Spring Security + Keycloak
3. ✅ Demostración: Llamadas en Postman validadas
4. ✅ Código: SecurityConfig.java y @PreAuthorize
5. ✅ Mejoras: 6 sugerencias para level-up la seguridad

El microservicio está securizado y listo para producción.

Preguntas o comentarios del equipo sobre la arquitectura 
o las mejoras propuestas."
```

---

## 🎬 Instrucciones Técnicas para Grabar

### **Software Recomendado**
- **OBS Studio** (gratuito, profesional)
- **ScreenFlow** (macOS)
- **Camtasia** (multiplataforma)

### **Configuración de OBS**
```
Resolución: 1920x1080 (Full HD)
FPS: 30
Códec: H.264
Bitrate: 5000 kbps (para buena calidad)
```

### **Preparación Previa**
```
1. ✅ Tener Keycloak ejecutándose localmente
   docker run -p 8080:8080 -e KEYCLOAK_ADMIN=admin \
     -e KEYCLOAK_ADMIN_PASSWORD=admin \
     quay.io/keycloak/keycloak:latest start-dev

2. ✅ Tener el microservicio ejecutándose en Spring Boot
   mvn spring-boot:run

3. ✅ Importar colección de Postman con peticiones pre-configuradas

4. ✅ Tener preparados los scripts (este documento)

5. ✅ Probar todos los demos antes de grabar "en serio"

6. ✅ Silenciar notificaciones y distracciones

7. ✅ Usar micrófono de buena calidad
```

### **Edición Post-Grabación**
```
1. Cortar silencios innecesarios
2. Agregar subtítulos (accesibilidad)
3. Resaltar puntos clave con highlights en pantalla
4. Agregar transiciones suaves entre secciones
5. Música de fondo baja (no distraiga)
6. Incluir títulos de secciones
```

### **Publicación**
```
Opciones:
- YouTube (privado o público según política de la empresa)
- Microsoft Stream (si usan Teams)
- Loom (para compartir rápido)
- OneDrive (acceso controlado)

Incluir en descripción:
- Timestamp de cada sección
- Enlaces a documentación
- Información de contacto del equipo
- Versiones de software usado
```

---

## 📑 Checklist Antes de Grabar

- [ ] Keycloak corriendo localmente
- [ ] Microservicio compilado y ejecutándose
- [ ] Postman con endpoints pre-configurados
- [ ] VS Code/IntelliJ con los archivos de código listos
- [ ] jwt.io abierto en el navegador
- [ ] Micrófono testado y funcionando
- [ ] OBS configurado y testado
- [ ] Scripts (estos de arriba) impresos o en segunda pantalla
- [ ] Base de datos con datos de prueba
- [ ] TODAS las demostraciones testadas manualmente
- [ ] Monitor en 1920x1080 o más (para texto legible)
- [ ] Iluminación adecuada
- [ ] Sin distracciones de sonido (notificaciones silenciadas)

---

## 🎯 Duración Final Esperada

| Sección | Duración |
|---------|----------|
| 1. Introducción | 1-2 min |
| 2. Arquitectura | 2-3 min |
| 3. Flujo Detallado | 2-3 min |
| 4. Demo en Postman | 4-5 min |
| 5. Código Fuente | 3-4 min |
| 6. Ventajas | 2 min |
| 7. Mejoras Propuestas | 2-3 min |
| 8. Cierre | 1 min |
| **TOTAL** | **~16-20 min** |

---

## 📚 Referencias

- [Spring Security OAuth2 Documentation](https://spring.io/projects/spring-security)
- [Keycloak Administration Guide](https://www.keycloak.org/documentation)
- [JWT Introduction](https://jwt.io/introduction)
- [JWT Decoder Online](https://jwt.io)
- [OWASP Authentication Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html)

---

## 📝 Notas Finales

Este documento es una **guía completa** para producir un video de calidad profesional que demuestre la seguridad del microservicio. Puedes personalizar los scripts según tu estilo y la audiencia específica.

El objetivo es demostrar:
1. **Que la seguridad está implementada correctamente**
2. **Cómo funciona en la práctica**
3. **Que el código es robusto y mantenible**
4. **Opciones de mejora pensadas estratégicamente**

¡Buena suerte con la grabación! 🎉
