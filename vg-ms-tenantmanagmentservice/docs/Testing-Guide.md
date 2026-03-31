#  Activar el backend

mvn spring-boot:run

# Guía de Pruebas de Seguridad

Esta guía describe cómo verificar la correcta implementación de la seguridad multi-emisor (Keycloak y Firebase) en el microservicio.

## 1. Verificación en Swagger UI (Interfaz Visual)
Al entrar a `http://localhost:5001/swagger-ui.html`, verás la lista de tus endpoints.

1.  **Probar endpoint público sin Token**: Haz clic en `GET /api/v1/municipalities`, presiona **"Try it out"** y luego **"Execute"**.
    *   **Resultado esperado**: Verás un `200 OK`. Esto confirma que el catálogo público está habilitado.
2.  **Probar endpoint protegido sin Token**: Ejecuta `POST /api/v1/municipalities` sin autenticación.
    *   **Resultado esperado**: Verás un error `401 Unauthorized`. Esto confirma que la seguridad sigue activa para escritura.
3.  **Probar con Token**: Verás un botón verde que dice **"Authorize"** (con un candado) en la parte superior derecha.
    *   Haz clic en él y pega tu token JWT (sin la palabra "Bearer", solo el código).
    *   Haz clic en **"Authorize"** y luego en **"Close"**.
    *   Ahora, vuelve a presionar **"Execute"** en un endpoint protegido.
    *   **Resultado esperado**: Verás un `200 OK` (siempre que el token sea válido para Keycloak o Firebase).

## 2. Verificación de Rutas Públicas (vía CURL)
Las rutas de Swagger y Actuator deben ser accesibles sin token.

## 2. Verificación de Rutas Públicas (Sin Token)
Los endpoints de lectura del catálogo deben responder sin autenticación.

```bash
curl -I http://localhost:5001/api/v1/municipalities
```
**Resultado esperado:** HTTP 200 OK.

## 3. Verificación de Rutas Protegidas (Sin Token)
Cualquier ruta de escritura debe ser rechazada si no se envía un token.

```bash
curl -i -X POST http://localhost:5001/api/v1/municipalities \
    -H "Content-Type: application/json" \
    -d '{}'
```
**Resultado esperado:** HTTP 401 Unauthorized.

## 4. Verificación con Token (Keycloak)
Para probar con Keycloak, necesitas un token JWT emitido por tu servidor.

```bash
TOKEN="tu_token_aqui"
curl -H "Authorization: Bearer $TOKEN" http://localhost:5001/api/v1/municipalities
```
**Resultado esperado:** HTTP 200 OK (si el emisor en el token coincide con la configuración).

## 5. Verificación con Token (Firebase)
De igual manera para Firebase:

```bash
TOKEN_FIREBASE="tu_token_firebase"
curl -H "Authorization: Bearer $TOKEN_FIREBASE" http://localhost:5001/api/v1/municipalities
```
**Resultado esperado:** HTTP 200 OK.

> [!TIP]
> Puedes usar [jwt.io](https://jwt.io) para inspeccionar tus tokens y verificar que el campo `iss` coincida exactamente con lo configurado en `application.yml`.
