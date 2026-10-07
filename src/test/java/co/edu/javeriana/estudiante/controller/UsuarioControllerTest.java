package co.edu.javeriana.estudiante.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.javeriana.estudiante.dto.UsuarioRequest;
import co.edu.javeriana.estudiante.dto.UsuarioResponse;
import co.edu.javeriana.estudiante.exception.RecursoDuplicadoException;
import co.edu.javeriana.estudiante.exception.RecursoEnUsoException;
import co.edu.javeriana.estudiante.exception.RecursoNoEncontradoException;
import co.edu.javeriana.estudiante.service.UsuarioService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UsuarioController.class)
class UsuarioControllerTest {

	private static final String JSON_VALIDO = """
			{
			  "nombre": "Ana Pérez",
			  "correo": "ana@javeriana.edu.co"
			}
			""";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UsuarioService usuarioService;

	private UsuarioResponse respuesta() {
		return UsuarioResponse.builder().id(1L).nombre("Ana Pérez").correo("ana@javeriana.edu.co").build();
	}

	@Test
	void crearDevuelve201ConLocation() throws Exception {
		given(usuarioService.crear(any(UsuarioRequest.class))).willReturn(respuesta());

		mockMvc.perform(post("/api/estudiante/usuarios").contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", "/api/estudiante/usuarios/1"))
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.correo").value("ana@javeriana.edu.co"));
	}

	@Test
	void crearSinCamposObligatoriosDevuelve400() throws Exception {
		mockMvc.perform(post("/api/estudiante/usuarios").contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.campos.nombre").exists())
				.andExpect(jsonPath("$.campos.correo").exists());

		verify(usuarioService, never()).crear(any());
	}

	@Test
	void crearConCorreoInvalidoDevuelve400() throws Exception {
		String cuerpo = """
				{"nombre":"Ana","correo":"no-es-correo"}
				""";

		mockMvc.perform(post("/api/estudiante/usuarios").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.campos.correo").value("El correo no tiene un formato válido"));
	}

	@Test
	void crearConCorreoRepetidoDevuelve409() throws Exception {
		willThrow(new RecursoDuplicadoException("Ya existe un usuario con el correo ana@javeriana.edu.co"))
				.given(usuarioService).crear(any(UsuarioRequest.class));

		mockMvc.perform(post("/api/estudiante/usuarios").contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409));
	}

	@Test
	void listarDevuelve200() throws Exception {
		given(usuarioService.listar()).willReturn(List.of(respuesta()));

		mockMvc.perform(get("/api/estudiante/usuarios"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].nombre").value("Ana Pérez"));
	}

	@Test
	void obtenerInexistenteDevuelve404() throws Exception {
		given(usuarioService.obtener(99L)).willThrow(new RecursoNoEncontradoException("No existe el usuario con id 99"));

		mockMvc.perform(get("/api/estudiante/usuarios/99"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("No existe el usuario con id 99"));
	}

	@Test
	void actualizarDevuelve200() throws Exception {
		given(usuarioService.actualizar(eq(1L), any(UsuarioRequest.class))).willReturn(respuesta());

		mockMvc.perform(put("/api/estudiante/usuarios/1").contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1));
	}

	@Test
	void eliminarDevuelve204() throws Exception {
		mockMvc.perform(delete("/api/estudiante/usuarios/1"))
				.andExpect(status().isNoContent());

		verify(usuarioService).eliminar(1L);
	}

	@Test
	void eliminarConClasesDevuelve409() throws Exception {
		willThrow(new RecursoEnUsoException("El usuario con id 1 tiene clases asociadas y no se puede eliminar"))
				.given(usuarioService).eliminar(1L);

		mockMvc.perform(delete("/api/estudiante/usuarios/1"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409));
	}

}
