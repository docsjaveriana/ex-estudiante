package co.edu.javeriana.estudiante.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Datos de entrada para crear o actualizar una clase. */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaseRequest {

	@NotBlank(message = "El código es obligatorio")
	private String codigo;

	@NotBlank(message = "El nombre es obligatorio")
	private String nombre;

	@NotNull(message = "Los créditos son obligatorios")
	@Positive(message = "Los créditos deben ser mayores que cero")
	private Integer creditos;

	private String semestre;

	@NotNull(message = "El usuario es obligatorio")
	private Long usuarioId;

}
