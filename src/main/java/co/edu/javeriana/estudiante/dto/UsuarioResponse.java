package co.edu.javeriana.estudiante.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Representación de un usuario expuesta por la API. */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponse {

	private Long id;

	private String nombre;

	private String correo;

}
