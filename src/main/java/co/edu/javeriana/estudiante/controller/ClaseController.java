package co.edu.javeriana.estudiante.controller;

import co.edu.javeriana.estudiante.dto.ClaseRequest;
import co.edu.javeriana.estudiante.dto.ClaseResponse;
import co.edu.javeriana.estudiante.service.ClaseService;
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

/** CRUD de clases. */
@Tag(name = "Clases", description = "CRUD de clases")
@RestController
@RequestMapping("/api/estudiante/clases")
@RequiredArgsConstructor
public class ClaseController {

	private final ClaseService claseService;

	@Operation(summary = "Crear una clase")
	@PostMapping
	public ResponseEntity<ClaseResponse> crear(@Valid @RequestBody ClaseRequest request) {
		ClaseResponse creada = claseService.crear(request);
		return ResponseEntity.created(URI.create("/api/estudiante/clases/" + creada.getId())).body(creada);
	}

	@Operation(summary = "Listar todas las clases")
	@GetMapping
	public ResponseEntity<List<ClaseResponse>> listar() {
		return ResponseEntity.ok(claseService.listar());
	}

	@Operation(summary = "Consultar una clase")
	@GetMapping("/{id}")
	public ResponseEntity<ClaseResponse> obtener(@PathVariable Long id) {
		return ResponseEntity.ok(claseService.obtener(id));
	}

	@Operation(summary = "Reemplazar una clase")
	@PutMapping("/{id}")
	public ResponseEntity<ClaseResponse> actualizar(@PathVariable Long id, @Valid @RequestBody ClaseRequest request) {
		return ResponseEntity.ok(claseService.actualizar(id, request));
	}

	@Operation(summary = "Eliminar una clase")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable Long id) {
		claseService.eliminar(id);
		return ResponseEntity.noContent().build();
	}

}
