package co.edu.javeriana.estudiante.service;

import co.edu.javeriana.estudiante.dto.UsuarioResponse;
import co.edu.javeriana.estudiante.model.Usuario;
import org.springframework.stereotype.Component;

/** Traduce la entidad {@link Usuario} a su representación de salida. */
@Component
public class UsuarioMapper {

	public UsuarioResponse aResponse(Usuario usuario) {
		return UsuarioResponse.builder()
				.id(usuario.getId())
				.nombre(usuario.getNombre())
				.correo(usuario.getCorreo())
				.build();
	}

}
