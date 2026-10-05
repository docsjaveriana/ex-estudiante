package co.edu.javeriana.estudiante.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Metadatos de la documentación OpenAPI que publica Swagger UI. */
@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI estudianteOpenApi() {
		return new OpenAPI().info(new Info()
				.title("API Estudiante")
				.description("CRUD de clases y consulta de las clases de un usuario.")
				.version("v1"));
	}

}
