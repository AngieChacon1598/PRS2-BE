package edu.pe.vallegrande.AuthenticationService;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "PORT=8080",
    "DATABASE_URL=r2dbc:h2:mem:///test;DB_CLOSE_DELAY=-1",
    "DB_USERNAME=sa",
    "DB_PASSWORD=",
    "KEYCLOAK_URL=http://localhost:8080",
    "KEYCLOAK_REALM=test",
    "KEYCLOAK_CLIENT_ID=test-client",
    "KEYCLOAK_CLIENT_SECRET=secret",
    "KEYCLOAK_ADMIN_SECRET=admin-secret",
    "CONFIGURATION_SERVICE_URL=http://localhost:8888",
    "CORS_ALLOWED_ORIGINS=*",
    "REDIS_HOST=localhost",
    "REDIS_PORT=6379",
    "JWT_SECRET=v9y$B&E)H@McQfTjWnZr4u7x!A%C*F-JaNdRgUkXp2s5v8y/B?E(G+KbPeShVmYp",
    "JWT_EXPIRATION=3600000",
    "JWT_REFRESH_EXPIRATION=604800000"
})
class AuthenticationServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
