package co.edu.javeriana.estudiante.exception;

import co.edu.javeriana.estudiante.dto.ErrorResponse;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Traduce las excepciones de dominio y de validación a códigos HTTP. */
@RestControllerAdvice
public class ManejadorGlobalExcepciones {

	@ExceptionHandler(RecursoNoEncontradoException.class)
	public ResponseEntity<ErrorResponse> manejarNoEncontrado(RecursoNoEncontradoException ex) {
		return construir(HttpStatus.NOT_FOUND, ex.getMessage(), null);
	}

	@ExceptionHandler(RecursoDuplicadoException.class)
	public ResponseEntity<ErrorResponse> manejarDuplicado(RecursoDuplicadoException ex) {
		return construir(HttpStatus.CONFLICT, ex.getMessage(), null);
	}

	/** Red de seguridad: una restricción de unicidad violada en carrera también es un 409. */
	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ErrorResponse> manejarIntegridad(DataIntegrityViolationException ex) {
		return construir(HttpStatus.CONFLICT, "La operación viola una restricción de integridad", null);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> manejarValidacion(MethodArgumentNotValidException ex) {
		Map<String, String> campos = new HashMap<>();
		for (FieldError error : ex.getBindingResult().getFieldErrors()) {
			campos.put(error.getField(), error.getDefaultMessage());
		}
		return construir(HttpStatus.BAD_REQUEST, "Los datos enviados no son válidos", campos);
	}

	private ResponseEntity<ErrorResponse> construir(HttpStatus estado, String mensaje, Map<String, String> campos) {
		ErrorResponse cuerpo = ErrorResponse.builder()
				.timestamp(OffsetDateTime.now(ZoneId.systemDefault()))
				.status(estado.value())
				.error(estado.getReasonPhrase())
				.message(mensaje)
				.campos(campos)
				.build();
		return ResponseEntity.status(estado).body(cuerpo);
	}

}
