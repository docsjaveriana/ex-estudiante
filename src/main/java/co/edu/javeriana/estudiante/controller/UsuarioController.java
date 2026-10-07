package co.edu.javeriana.estudiante.controller;

import co.edu.javeriana.estudiante.dto.UsuarioRequest;
import co.edu.javeriana.estudiante.dto.UsuarioResponse;
import co.edu.javeriana.estudiante.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** CRUD de usuarios (estudiantes). */
@Tag(name = "Usuarios", description = "CRUD de usuarios")
@RestController
@RequestMapping("/api/estudiante/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

	private final UsuarioService usuarioService;

	@Operation(summary = "Crear un usuario")
	@PostMapping
	public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioRequest request) {
		UsuarioResponse creado = usuarioService.crear(request);
		return ResponseEntity.created(URI.create("/api/estudiante/usuarios/" + creado.getId())).body(creado);
	}

	@Operation(summary = "Listar todos los usuarios")
	@GetMapping
	public ResponseEntity<List<UsuarioResponse>> listar() {
		return ResponseEntity.ok(usuarioService.listar());
	}

	@Operation(summary = "Consultar un usuario")
	@GetMapping("/{id}")
	public ResponseEntity<UsuarioResponse> obtener(@PathVariable Long id) {
		return ResponseEntity.ok(usuarioService.obtener(id));
	}

	@Operation(summary = "Reemplazar un usuario")
	@PutMapping("/{id}")
	public ResponseEntity<UsuarioResponse> actualizar(@PathVariable Long id, @Valid @RequestBody UsuarioRequest request) {
		return ResponseEntity.ok(usuarioService.actualizar(id, request));
	}

	@Operation(summary = "Eliminar un usuario sin clases asociadas")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable Long id) {
		usuarioService.eliminar(id);
		return ResponseEntity.noContent().build();
	}

}
