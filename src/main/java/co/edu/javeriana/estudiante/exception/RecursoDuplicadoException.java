package co.edu.javeriana.estudiante.exception;

/** Se intenta crear o actualizar un recurso violando una restricción de unicidad. Se traduce a HTTP 409. */
public class RecursoDuplicadoException extends RuntimeException {

	public RecursoDuplicadoException(String mensaje) {
		super(mensaje);
	}

}
