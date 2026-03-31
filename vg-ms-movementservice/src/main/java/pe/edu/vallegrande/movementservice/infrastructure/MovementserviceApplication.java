package pe.edu.vallegrande.movementservice.infrastructure;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import javax.net.ssl.HttpsURLConnection;

@SpringBootApplication
@ComponentScan(basePackages = "pe.edu.vallegrande.movementservice")
@EnableR2dbcRepositories(basePackages = "pe.edu.vallegrande.movementservice.infrastructure.adapters.output.persistence")
@EntityScan(basePackages = "pe.edu.vallegrande.movementservice.domain.model")
public class MovementserviceApplication {

	public static void main(String[] args) {
		HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
		SpringApplication.run(MovementserviceApplication.class, args);
	}

}
