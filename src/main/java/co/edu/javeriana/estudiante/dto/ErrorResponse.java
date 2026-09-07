package co.edu.javeriana.estudiante.dto;

import java.time.OffsetDateTime;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Cuerpo uniforme de las respuestas de error. */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

	private OffsetDateTime timestamp;

	private int status;

	private String error;

	private String message;

	/** Errores por campo; solo presente en fallos de validación. */
	private Map<String, String> campos;

}
