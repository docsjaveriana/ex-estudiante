package co.edu.javeriana.estudiante.service;

import co.edu.javeriana.estudiante.dto.ClaseResponse;
import co.edu.javeriana.estudiante.model.Clase;
import org.springframework.stereotype.Component;

/** Traduce la entidad {@link Clase} a su representación de salida. */
@Component
public class ClaseMapper {

	/**
	 * Lee solo el identificador del usuario, disponible en el proxy LAZY sin
	 * disparar una consulta adicional.
	 */
	public ClaseResponse aResponse(Clase clase) {
		return ClaseResponse.builder()
				.id(clase.getId())
				.codigo(clase.getCodigo())
				.nombre(clase.getNombre())
				.creditos(clase.getCreditos())
				.semestre(clase.getSemestre())
				.usuarioId(clase.getUsuario().getId())
				.build();
	}

}
