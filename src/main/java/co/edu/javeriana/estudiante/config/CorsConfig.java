package co.edu.javeriana.estudiante.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Permite que el frontend Angular, servido desde otro origen (p. ej. ng serve en
 * http://localhost:4200), llame directamente a la API.
 *
 * <p>Los orígenes se configuran con {@code app.cors.allowed-origins} (variable
 * {@code CORS_ALLOWED_ORIGINS}, separados por coma). Por defecto es {@code *}: cualquier
 * origen, válido porque la API no usa cookies ni credenciales.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

	private final String[] origenesPermitidos;

	public CorsConfig(@Value("${app.cors.allowed-origins}") String[] origenesPermitidos) {
		this.origenesPermitidos = origenesPermitidos;
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/api/estudiante/**")
				.allowedOrigins(origenesPermitidos)
				.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
				.allowedHeaders("*")
				.exposedHeaders("Location");
	}

}
