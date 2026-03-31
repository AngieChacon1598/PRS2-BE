# Documentación de Seguridad Microservicio `vg-ms-tenantmanagmentservice`

Esta documentación detalla la implementación realizada para securizar el backend siguiendo los estándares de **Spring Boot 3**, **Keycloak** y **Firebase**.

## 1. Arquitectura de Seguridad
Hemos implementado un modelo de **Servidor de Recursos OAuth2** reactivo (WebFlux). Esto significa que el microservicio no gestiona usuarios ni contraseñas, sino que valida "pases" (tokens JWT) emitidos por proveedores externos.

### Componentes Clave:
*   **Multi-Emisor**: La aplicación puede aceptar y validar tokens tanto de Keycloak como de Firebase simultáneamente.
*   **Resolución Dinámica**: El sistema lee el emisor (`iss`) del token y decide qué proveedor usar para validarlo.
*   **Carga Perezosa (Lazy)**: Los validadores se conectan a los servidores de identidad solo cuando se recibe el primer token, evitando que la app falle si los servidores están caídos al arrancar.

---

## 2. Cambios en el Proyecto

### A. Dependencias (`pom.xml`)
Añadimos las librerías necesarias para que Spring entienda de seguridad y tokens:
*   `spring-boot-starter-security`: Activa el motor de seguridad.
*   `spring-boot-starter-oauth2-resource-server`: Permite validar tokens JWT.
*   `spring-boot-starter-validation`: Para asegurar que los datos de entrada sean correctos.

### B. Configuración (`application.yml`)
Definimos las URLs de los servidores de identidad:
*   **Keycloak**: URL del Realm configurado.
*   **Firebase**: URL basada en tu Project ID de Google.

### C. Lógica de Seguridad (`SecurityConfig.java`)
Es el corazón de la protección. Su función es:
1.  **Reglas de acceso**:
    *   Rutas libres: `/swagger-ui.html`, `/actuator/**` y los `GET` de catálogo en `/api/v1/municipalities/**`.
    *   Rutas protegidas: Operaciones de escritura y orquestación (`POST`, `PUT`, `DELETE` y endpoints que requieren token explícito).
2.  **Resolutor de Emisores**: Usa `ReactiveAuthenticationManagerResolver` para elegir entre Keycloak y Firebase dinámicamente.

### D. Documentación Interactiva (`OpenApiConfig.java`)
Añadimos soporte para que Swagger UI muestre el botón **"Authorize"**. Esto permite pegar el token JWT directamente en el navegador para probar los endpoints sin usar herramientas externas como Postman.

---

## 3. ¿Cómo funciona el flujo?
1.  El cliente (Frontend o Postman) envía una petición con un encabezado: `Authorization: Bearer <TOKEN>`.
2.  `SecurityConfig` detecta el token y extrae el emisor.
3.  Si es de Keycloak, descarga las llaves públicas de Keycloak y valida la firma del token.
4.  Si la firma es válida, permite el acceso al controlador correspondiente.
5.  Si una ruta protegida no recibe token o el token es inválido, devuelve un error **401 Unauthorized**.

---

## 4. Archivos de Referencia
*   [SecurityConfig.java](file:///home/erikyalli/PRS/vg-ms-tenantmanagmentservice/src/main/java/pe/edu/vallegrande/configurationservice/config/SecurityConfig.java): Lógica principal de validación.
*   [application.yml](file:///home/erikyalli/PRS/vg-ms-tenantmanagmentservice/src/main/resources/application.yml): Parámetros de conexión.
*   [Testing-Guide.md](file:///home/erikyalli/PRS/vg-ms-tenantmanagmentservice/docs/Testing-Guide.md): Pasos para probarlo tú mismo.
