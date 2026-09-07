package co.edu.javeriana.estudiante.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Asignatura que cursa un usuario. Única entidad con CRUD completo.
 *
 * <p>Una clase pertenece siempre a un usuario; no existen clases huérfanas.
 */
@Entity
@Table(name = "clase")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Clase {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String codigo;

	@Column(nullable = false)
	private String nombre;

	@Column(nullable = false)
	private Integer creditos;

	@Column
	private String semestre;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "usuario_id", nullable = false)
	private Usuario usuario;

}
