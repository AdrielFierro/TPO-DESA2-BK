package com.uade.comedor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import jakarta.annotation.PostConstruct;
import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
public class ComedorApplication {

	@PostConstruct
	public void init() {
		// Configurar la zona horaria de Argentina para toda la aplicación
		TimeZone.setDefault(TimeZone.getTimeZone("America/Argentina/Buenos_Aires"));
	}

	public static void main(String[] args) {
		SpringApplication.run(ComedorApplication.class, args);
	}

}
