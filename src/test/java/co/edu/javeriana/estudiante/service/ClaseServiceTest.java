package co.edu.javeriana.estudiante.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.javeriana.estudiante.dto.ClaseRequest;
import co.edu.javeriana.estudiante.dto.ClaseResponse;
import co.edu.javeriana.estudiante.exception.RecursoDuplicadoException;
import co.edu.javeriana.estudiante.exception.RecursoNoEncontradoException;
import co.edu.javeriana.estudiante.model.Clase;
import co.edu.javeriana.estudiante.model.Usuario;
import co.edu.javeriana.estudiante.repository.ClaseRepository;
import co.edu.javeriana.estudiante.repository.UsuarioRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ClaseServiceTest {

	@Mock
	private ClaseRepository claseRepository;

	@Mock
	private UsuarioRepository usuarioRepository;

	private ClaseService claseService;

	private Usuario usuario;

	@BeforeEach
	void setUp() {
		// El mapper es lógica pura: se usa el real para cubrir también la traducción a DTO.
		claseService = new ClaseService(claseRepository, usuarioRepository, new ClaseMapper());
		usuario = Usuario.builder().id(1L).nombre("Ana").correo("ana@javeriana.edu.co").build();
	}

	private ClaseRequest request() {
		return ClaseRequest.builder()
				.codigo("ISIS-1101")
				.nombre("Algoritmos")
				.creditos(3)
				.semestre("2026-1")
				.usuarioId(1L)
				.build();
	}

	private Clase clasePersistida() {
		return Clase.builder()
				.id(10L)
				.codigo("ISIS-1101")
				.nombre("Algoritmos")
				.creditos(3)
				.semestre("2026-1")
				.usuario(usuario)
				.build();
	}

	@Test
	void crearDevuelveLaClaseGuardada() {
		when(claseRepository.existsByCodigo("ISIS-1101")).thenReturn(false);
		when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
		when(claseRepository.save(any(Clase.class))).thenReturn(clasePersistida());

		ClaseResponse respuesta = claseService.crear(request());

		assertThat(respuesta.getId()).isEqualTo(10L);
		assertThat(respuesta.getCodigo()).isEqualTo("ISIS-1101");
		assertThat(respuesta.getCreditos()).isEqualTo(3);
		assertThat(respuesta.getUsuarioId()).isEqualTo(1L);
	}

	@Test
	void crearConCodigoRepetidoFalla() {
		when(claseRepository.existsByCodigo("ISIS-1101")).thenReturn(true);
		ClaseRequest request = request();

		assertThatThrownBy(() -> claseService.crear(request))
				.isInstanceOf(RecursoDuplicadoException.class)
				.hasMessageContaining("ISIS-1101");

		verify(claseRepository, never()).save(any());
	}

	@Test
	void crearConUsuarioInexistenteFalla() {
		when(claseRepository.existsByCodigo("ISIS-1101")).thenReturn(false);
		when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());
		ClaseRequest request = request();

		assertThatThrownBy(() -> claseService.crear(request))
				.isInstanceOf(RecursoNoEncontradoException.class)
				.hasMessageContaining("usuario");

		verify(claseRepository, never()).save(any());
	}

	@Test
	void obtenerClaseInexistenteFalla() {
		when(claseRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> claseService.obtener(99L))
				.isInstanceOf(RecursoNoEncontradoException.class)
				.hasMessageContaining("99");
	}

	@Test
	void listarDevuelveTodasLasClases() {
		when(claseRepository.findAll()).thenReturn(List.of(clasePersistida()));

		assertThat(claseService.listar()).hasSize(1);
	}

	@Test
	void listarPorUsuarioInexistenteFalla() {
		when(usuarioRepository.existsById(99L)).thenReturn(false);

		assertThatThrownBy(() -> claseService.listarPorUsuario(99L))
				.isInstanceOf(RecursoNoEncontradoException.class);

		verify(claseRepository, never()).findByUsuarioId(any());
	}

	@Test
	void listarPorUsuarioDevuelveSusClases() {
		when(usuarioRepository.existsById(1L)).thenReturn(true);
		when(claseRepository.findByUsuarioId(1L)).thenReturn(List.of(clasePersistida()));

		List<ClaseResponse> clases = claseService.listarPorUsuario(1L);

		assertThat(clases).singleElement().satisfies(c -> assertThat(c.getUsuarioId()).isEqualTo(1L));
	}

	@Test
	void actualizarReemplazaTodosLosCampos() {
		Clase existente = clasePersistida();
		when(claseRepository.findById(10L)).thenReturn(Optional.of(existente));
		when(claseRepository.existsByCodigoAndIdNot("ISIS-2202", 10L)).thenReturn(false);
		when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
		when(claseRepository.save(any(Clase.class))).thenAnswer(inv -> inv.getArgument(0));

		ClaseRequest cambio = ClaseRequest.builder()
				.codigo("ISIS-2202")
				.nombre("Estructuras de Datos")
				.creditos(4)
				.semestre("2026-2")
				.usuarioId(1L)
				.build();

		ClaseResponse respuesta = claseService.actualizar(10L, cambio);

		assertThat(respuesta.getCodigo()).isEqualTo("ISIS-2202");
		assertThat(respuesta.getNombre()).isEqualTo("Estructuras de Datos");
		assertThat(respuesta.getCreditos()).isEqualTo(4);
		assertThat(respuesta.getSemestre()).isEqualTo("2026-2");
	}

	@Test
	void actualizarConCodigoDeOtraClaseFalla() {
		when(claseRepository.findById(10L)).thenReturn(Optional.of(clasePersistida()));
		when(claseRepository.existsByCodigoAndIdNot("ISIS-1101", 10L)).thenReturn(true);
		ClaseRequest request = request();

		assertThatThrownBy(() -> claseService.actualizar(10L, request))
				.isInstanceOf(RecursoDuplicadoException.class);

		verify(claseRepository, never()).save(any());
	}

	@Test
	void actualizarClaseInexistenteFalla() {
		when(claseRepository.findById(99L)).thenReturn(Optional.empty());
		ClaseRequest request = request();

		assertThatThrownBy(() -> claseService.actualizar(99L, request))
				.isInstanceOf(RecursoNoEncontradoException.class);
	}

	@Test
	void eliminarBorraLaClase() {
		Clase existente = clasePersistida();
		when(claseRepository.findById(10L)).thenReturn(Optional.of(existente));

		claseService.eliminar(10L);

		verify(claseRepository).delete(existente);
	}

	@Test
	void eliminarClaseInexistenteFalla() {
		when(claseRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> claseService.eliminar(99L))
				.isInstanceOf(RecursoNoEncontradoException.class);

		verify(claseRepository, never()).delete(any());
	}

}
