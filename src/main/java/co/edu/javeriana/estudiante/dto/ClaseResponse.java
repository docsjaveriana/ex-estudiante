package co.edu.javeriana.estudiante.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Representación de una clase expuesta por la API. */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaseResponse {

	private Long id;

	private String codigo;

	private String nombre;

	private Integer creditos;

	private String semestre;

	private Long usuarioId;

}
