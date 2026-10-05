package co.edu.javeriana.estudiante.controller;

import co.edu.javeriana.estudiante.dto.ClaseResponse;
import co.edu.javeriana.estudiante.service.ClaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Clases de un usuario concreto.
 *
 * <p>Solo lectura: el usuario no tiene CRUD propio.
 */
@Tag(name = "Clases por usuario", description = "Clases de un usuario concreto")
@RestController
@RequestMapping("/api/estudiante/usuarios/{usuarioId}/clases")
@RequiredArgsConstructor
public class UsuarioClaseController {

	private final ClaseService claseService;

	@Operation(summary = "Listar las clases de un usuario")
	@GetMapping
	public ResponseEntity<List<ClaseResponse>> listarPorUsuario(@PathVariable Long usuarioId) {
		return ResponseEntity.ok(claseService.listarPorUsuario(usuarioId));
	}

}
