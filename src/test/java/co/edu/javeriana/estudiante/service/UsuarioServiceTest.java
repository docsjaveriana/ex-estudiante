package co.edu.javeriana.estudiante.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.javeriana.estudiante.dto.UsuarioRequest;
import co.edu.javeriana.estudiante.dto.UsuarioResponse;
import co.edu.javeriana.estudiante.exception.RecursoDuplicadoException;
import co.edu.javeriana.estudiante.exception.RecursoEnUsoException;
import co.edu.javeriana.estudiante.exception.RecursoNoEncontradoException;
import co.edu.javeriana.estudiante.model.Usuario;
import co.edu.javeriana.estudiante.repository.ClaseRepository;
import co.edu.javeriana.estudiante.repository.UsuarioRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

	@Mock
	private UsuarioRepository usuarioRepository;

	@Mock
	private ClaseRepository claseRepository;

	private UsuarioService usuarioService;

	@BeforeEach
	void setUp() {
		// El mapper es lógica pura: se usa el real para cubrir también la traducción a DTO.
		usuarioService = new UsuarioService(usuarioRepository, claseRepository, new UsuarioMapper());
	}

	private UsuarioRequest request() {
		return UsuarioRequest.builder().nombre("  Ana Pérez ").correo(" Ana@Javeriana.edu.co ").build();
	}

	private Usuario usuarioPersistido() {
		return Usuario.builder().id(1L).nombre("Ana Pérez").correo("ana@javeriana.edu.co").build();
	}

	@Test
	void crearNormalizaNombreYCorreo() {
		when(usuarioRepository.existsByCorreo("ana@javeriana.edu.co")).thenReturn(false);
		when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioPersistido());

		UsuarioResponse respuesta = usuarioService.crear(request());

		ArgumentCaptor<Usuario> guardado = ArgumentCaptor.forClass(Usuario.class);
		verify(usuarioRepository).save(guardado.capture());
		assertThat(guardado.getValue().getNombre()).isEqualTo("Ana Pérez");
		assertThat(guardado.getValue().getCorreo()).isEqualTo("ana@javeriana.edu.co");
		assertThat(respuesta.getId()).isEqualTo(1L);
	}

	@Test
	void crearConCorreoRepetidoLanzaDuplicado() {
		when(usuarioRepository.existsByCorreo("ana@javeriana.edu.co")).thenReturn(true);
		UsuarioRequest datos = request();

		assertThatThrownBy(() -> usuarioService.crear(datos))
				.isInstanceOf(RecursoDuplicadoException.class);
		verify(usuarioRepository, never()).save(any());
	}

	@Test
	void listarOrdenaPorNombre() {
		when(usuarioRepository.findAll(Sort.by("nombre"))).thenReturn(List.of(usuarioPersistido()));

		List<UsuarioResponse> respuesta = usuarioService.listar();

		assertThat(respuesta).extracting(UsuarioResponse::getCorreo).containsExactly("ana@javeriana.edu.co");
	}

	@Test
	void obtenerInexistenteLanzaNoEncontrado() {
		when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> usuarioService.obtener(99L))
				.isInstanceOf(RecursoNoEncontradoException.class);
	}

	@Test
	void actualizarReemplazaLosCampos() {
		Usuario existente = usuarioPersistido();
		when(usuarioRepository.findById(1L)).thenReturn(Optional.of(existente));
		when(usuarioRepository.existsByCorreoAndIdNot("ana.perez@javeriana.edu.co", 1L)).thenReturn(false);
		when(usuarioRepository.save(existente)).thenReturn(existente);
		UsuarioRequest datos = UsuarioRequest.builder().nombre("Ana María").correo("ana.perez@javeriana.edu.co").build();

		UsuarioResponse respuesta = usuarioService.actualizar(1L, datos);

		assertThat(respuesta.getNombre()).isEqualTo("Ana María");
		assertThat(respuesta.getCorreo()).isEqualTo("ana.perez@javeriana.edu.co");
	}

	@Test
	void actualizarConCorreoDeOtroUsuarioLanzaDuplicado() {
		when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioPersistido()));
		when(usuarioRepository.existsByCorreoAndIdNot("ana@javeriana.edu.co", 1L)).thenReturn(true);
		UsuarioRequest datos = request();

		assertThatThrownBy(() -> usuarioService.actualizar(1L, datos))
				.isInstanceOf(RecursoDuplicadoException.class);
		verify(usuarioRepository, never()).save(any());
	}

	@Test
	void eliminarSinClasesBorraElUsuario() {
		Usuario existente = usuarioPersistido();
		when(usuarioRepository.findById(1L)).thenReturn(Optional.of(existente));
		when(claseRepository.existsByUsuarioId(1L)).thenReturn(false);

		usuarioService.eliminar(1L);

		verify(usuarioRepository).delete(existente);
	}

	@Test
	void eliminarConClasesLanzaEnUso() {
		when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioPersistido()));
		when(claseRepository.existsByUsuarioId(1L)).thenReturn(true);

		assertThatThrownBy(() -> usuarioService.eliminar(1L))
				.isInstanceOf(RecursoEnUsoException.class);
		verify(usuarioRepository, never()).delete(any());
	}

}
