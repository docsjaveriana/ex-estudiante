package co.edu.javeriana.estudiante.exception;

/** Se intenta eliminar un recurso del que dependen otros. Se traduce a HTTP 409. */
public class RecursoEnUsoException extends RuntimeException {

	public RecursoEnUsoException(String mensaje) {
		super(mensaje);
	}

}
