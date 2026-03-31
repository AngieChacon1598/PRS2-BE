package pe.edu.vallegrande.configurationservice;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.test.StepVerifier;

@Disabled("Requiere conexión a base de datos")
@SpringBootTest
class ConnectionTest {

    @Autowired
    private DatabaseClient databaseClient;

    @Test
    void testDatabaseConnection() {
        StepVerifier.create(
                databaseClient.sql("SELECT 1").fetch().first()
        )
        .expectNextCount(1)
        .verifyComplete();
    }
}
