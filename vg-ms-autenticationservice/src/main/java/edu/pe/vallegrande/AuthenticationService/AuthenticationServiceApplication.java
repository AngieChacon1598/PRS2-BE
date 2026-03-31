package edu.pe.vallegrande.AuthenticationService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AuthenticationServiceApplication {

	static {
		// Forzar el uso del resolvedor de DNS del sistema (JDK) para evitar errores en Windows/Netty globally
		System.setProperty("io.netty.resolver.dns.nameServerProvider", 
			"io.netty.resolver.dns.DnsServerAddressStreamProviders#platformDefault");
	}

	public static void main(String[] args) {
		SpringApplication.run(AuthenticationServiceApplication.class, args);
	}

}
