# 📁 Estructura: `vg-ms-autenticationservice`

Arquitectura **Hexagonal (Ports & Adapters)** con Spring WebFlux (reactivo).

```
vg-ms-autenticationservice/
├── Dockerfile
├── HELP.md
├── README.md
├── mvnw
├── mvnw.cmd
├── pom.xml
├── logs/
│   └── authentication-service.log
│
└── src/
    └── main/
        ├── java/edu/pe/vallegrande/AuthenticationService/
        │   │
        │   ├── AuthenticationServiceApplication.java          ← Entry point
        │   │
        │   ├── application/                                   ── CAPA DE APLICACIÓN
        │   │   ├── service/
        │   │   │   ├── AssignmentServiceImpl.java
        │   │   │   ├── AuthServiceImpl.java
        │   │   │   ├── JwtServiceImpl.java
        │   │   │   ├── PermissionServiceImpl.java
        │   │   │   ├── PersonServiceImpl.java
        │   │   │   ├── RoleServiceImpl.java
        │   │   │   └── UserServiceImpl.java
        │   │   └── util/
        │   │       └── DateTimeUtil.java
        │   │
        │   ├── domain/                                        ── CAPA DE DOMINIO
        │   │   ├── exception/
        │   │   │   ├── DuplicateResourceException.java
        │   │   │   └── ResourceNotFoundException.java
        │   │   │
        │   │   ├── model/
        │   │   │   ├── assignment/
        │   │   │   │   ├── AssignRoleCommand.java
        │   │   │   │   ├── RolePermissionAssignment.java
        │   │   │   │   ├── RolePermissionLink.java
        │   │   │   │   ├── UserRoleAssignment.java
        │   │   │   │   └── UserRoleLink.java
        │   │   │   ├── auth/
        │   │   │   │   ├── AuthTokens.java
        │   │   │   │   ├── LoginCommand.java
        │   │   │   │   ├── LoginFailureInfo.java
        │   │   │   │   ├── LoginResult.java
        │   │   │   │   ├── RefreshTokenCommand.java
        │   │   │   │   └── UserPermission.java
        │   │   │   ├── permission/
        │   │   │   │   └── PermissionModel.java
        │   │   │   ├── person/
        │   │   │   │   ├── CreatePersonCommand.java
        │   │   │   │   ├── Person.java
        │   │   │   │   └── UpdatePersonCommand.java
        │   │   │   ├── role/
        │   │   │   │   ├── RoleModel.java
        │   │   │   │   └── UpsertRoleCommand.java
        │   │   │   └── user/
        │   │   │       ├── BlockUserCommand.java
        │   │   │       ├── CreateUserCommand.java
        │   │   │       ├── SuspendUserCommand.java
        │   │   │       ├── UpdateUserCommand.java
        │   │   │       └── UserAccount.java
        │   │   │
        │   │   └── ports/
        │   │       ├── in/                                    ← Puertos de entrada (interfaces de servicio)
        │   │       │   ├── AssignmentService.java
        │   │       │   ├── AuthService.java
        │   │       │   ├── JwtService.java
        │   │       │   ├── PermissionService.java
        │   │       │   ├── PersonService.java
        │   │       │   ├── RoleService.java
        │   │       │   └── UserService.java
        │   │       └── out/                                   ← Puertos de salida (interfaces de repositorio/seguridad)
        │   │           ├── AssignmentPermissionQueryPort.java
        │   │           ├── AuthPermissionPort.java
        │   │           ├── AuthUserPort.java
        │   │           ├── CurrentUserPort.java
        │   │           ├── PermissionPort.java
        │   │           ├── PersonPort.java
        │   │           ├── RolePermissionPort.java
        │   │           ├── RolePort.java
        │   │           ├── TokenBlacklistPort.java
        │   │           ├── UserPort.java
        │   │           └── UserRolePort.java
        │   │
        │   └── infrastructure/                                ── CAPA DE INFRAESTRUCTURA
        │       ├── adapter/
        │       │   ├── in/
        │       │   │   └── web/
        │       │   │       ├── controller/
        │       │   │       │   ├── AssignmentController.java
        │       │   │       │   ├── AuthController.java
        │       │   │       │   ├── PermissionController.java
        │       │   │       │   ├── PersonController.java
        │       │   │       │   ├── RoleController.java
        │       │   │       │   └── UserController.java
        │       │   │       ├── dto/
        │       │   │       │   ├── AssignRoleRequestDto.java
        │       │   │       │   ├── BlockUserRequestDto.java
        │       │   │       │   ├── LoginRequestDto.java
        │       │   │       │   ├── LoginResponseDto.java
        │       │   │       │   ├── PermissionRequestDto.java
        │       │   │       │   ├── PermissionResponseDto.java
        │       │   │       │   ├── PersonRequestDto.java
        │       │   │       │   ├── PersonResponseDto.java
        │       │   │       │   ├── RefreshTokenRequestDto.java
        │       │   │       │   ├── RolePermissionAssignmentDto.java
        │       │   │       │   ├── RoleRequestDto.java
        │       │   │       │   ├── RoleResponseDto.java
        │       │   │       │   ├── SuspendUserRequestDto.java
        │       │   │       │   ├── TokenResponseDto.java
        │       │   │       │   ├── UserCreateRequestDto.java
        │       │   │       │   ├── UserResponseDto.java
        │       │   │       │   ├── UserRoleAssignmentDto.java
        │       │   │       │   ├── UserUpdateRequestDto.java
        │       │   │       │   └── validation/
        │       │   │       │       ├── MinimumAge.java
        │       │   │       │       └── MinimumAgeValidator.java
        │       │   │       └── mapper/
        │       │   │           ├── AssignmentWebMapper.java
        │       │   │           ├── AuthWebMapper.java
        │       │   │           ├── PersonWebMapper.java
        │       │   │           ├── RoleWebMapper.java
        │       │   │           └── UserWebMapper.java
        │       │   │
        │       │   └── out/
        │       │       ├── persistence/
        │       │       │   ├── AssignmentPersistenceAdapter.java
        │       │       │   ├── AuthPersistenceAdapter.java
        │       │       │   ├── PermissionPersistenceAdapter.java
        │       │       │   ├── PersonPersistenceAdapter.java
        │       │       │   ├── RolePersistenceAdapter.java
        │       │       │   ├── UserPersistenceAdapter.java
        │       │       │   ├── entity/
        │       │       │   │   ├── Permission.java
        │       │       │   │   ├── Person.java
        │       │       │   │   ├── Role.java
        │       │       │   │   ├── RolePermission.java
        │       │       │   │   ├── User.java
        │       │       │   │   └── UserRole.java
        │       │       │   ├── mapper/
        │       │       │   │   ├── PermissionMapper.java
        │       │       │   │   ├── PersonPersistenceMapper.java
        │       │       │   │   ├── RoleMapper.java
        │       │       │   │   ├── RolePermissionMapper.java
        │       │       │   │   ├── UserAccountMapper.java
        │       │       │   │   └── UserRoleMapper.java
        │       │       │   └── repository/
        │       │       │       ├── PermissionRepository.java
        │       │       │       ├── PersonRepository.java
        │       │       │       ├── RolePermissionRepository.java
        │       │       │       ├── RoleRepository.java
        │       │       │       ├── UserRepository.java
        │       │       │       └── UserRoleRepository.java
        │       │       └── security/
        │       │           ├── InMemoryTokenBlacklistAdapter.java
        │       │           └── SecurityContextCurrentUserAdapter.java
        │       │
        │       └── config/
        │           ├── CorsConfig.java
        │           ├── JacksonConfig.java
        │           ├── R2dbcConfig.java
        │           └── SwaggerConfig.java
        │
        └── resources/
            ├── application.yml
            ├── META-INF/
            │   └── additional-spring-configuration-metadata.json
            └── db/
                ├── schema.sql
                ├── data.sql
                ├── migration_add_suspension_fields.sql
                └── migration_remove_middlename.sql
```

---

## Resumen de capas

| Capa | Paquete | Responsabilidad |
|---|---|---|
| **Domain** | `domain/model` | Entidades y comandos del negocio |
| **Domain** | `domain/ports/in` | Interfaces de servicios (contratos de entrada) |
| **Domain** | `domain/ports/out` | Interfaces de repositorios/seguridad (contratos de salida) |
| **Application** | `application/service` | Implementación de la lógica de negocio |
| **Infrastructure In** | `infrastructure/adapter/in/web` | Controllers REST, DTOs, Mappers web |
| **Infrastructure Out** | `infrastructure/adapter/out/persistence` | Adapters R2DBC, Entities, Repositories reactivos |
| **Infrastructure Out** | `infrastructure/adapter/out/security` | Blacklist de tokens, contexto de usuario |
| **Infrastructure** | `infrastructure/config` | CORS, Jackson, R2DBC, Swagger |
