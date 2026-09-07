package co.edu.javeriana.estudiante.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.javeriana.estudiante.dto.ClaseRequest;
import co.edu.javeriana.estudiante.dto.ClaseResponse;
import co.edu.javeriana.estudiante.exception.RecursoDuplicadoException;
import co.edu.javeriana.estudiante.exception.RecursoNoEncontradoException;
import co.edu.javeriana.estudiante.service.ClaseService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ClaseController.class)
class ClaseControllerTest {

	private static final String JSON_VALIDO = """
			{
			  "codigo": "ISIS-1101",
			  "nombre": "Algoritmos",
			  "creditos": 3,
			  "semestre": "2026-1",
			  "usuarioId": 1
			}
			""";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ClaseService claseService;

	private ClaseResponse respuesta() {
		return ClaseResponse.builder()
				.id(10L)
				.codigo("ISIS-1101")
				.nombre("Algoritmos")
				.creditos(3)
				.semestre("2026-1")
				.usuarioId(1L)
				.build();
	}

	@Test
	void crearDevuelve201ConLocation() throws Exception {
		given(claseService.crear(any(ClaseRequest.class))).willReturn(respuesta());

		mockMvc.perform(post("/api/clases").contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", "/api/clases/10"))
				.andExpect(jsonPath("$.id").value(10))
				.andExpect(jsonPath("$.codigo").value("ISIS-1101"))
				.andExpect(jsonPath("$.usuarioId").value(1));
	}

	@Test
	void crearConCreditosNoPositivosDevuelve400() throws Exception {
		String cuerpo = """
				{"codigo":"ISIS-1101","nombre":"Algoritmos","creditos":0,"usuarioId":1}
				""";

		mockMvc.perform(post("/api/clases").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.campos.creditos").exists());

		verify(claseService, never()).crear(any());
	}

	@Test
	void crearSinCamposObligatoriosDevuelve400() throws Exception {
		mockMvc.perform(post("/api/clases").contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.campos.codigo").exists())
				.andExpect(jsonPath("$.campos.nombre").exists())
				.andExpect(jsonPath("$.campos.creditos").exists())
				.andExpect(jsonPath("$.campos.usuarioId").exists());
	}

	@Test
	void crearConUsuarioInexistenteDevuelve404() throws Exception {
		willThrow(new RecursoNoEncontradoException("No existe el usuario con id 1"))
				.given(claseService).crear(any(ClaseRequest.class));

		mockMvc.perform(post("/api/clases").contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404));
	}

	@Test
	void crearConCodigoRepetidoDevuelve409() throws Exception {
		willThrow(new RecursoDuplicadoException("Ya existe una clase con el código ISIS-1101"))
				.given(claseService).crear(any(ClaseRequest.class));

		mockMvc.perform(post("/api/clases").contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409));
	}

	@Test
	void listarDevuelve200() throws Exception {
		given(claseService.listar()).willReturn(List.of(respuesta()));

		mockMvc.perform(get("/api/clases"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].codigo").value("ISIS-1101"));
	}

	@Test
	void obtenerDevuelve200() throws Exception {
		given(claseService.obtener(10L)).willReturn(respuesta());

		mockMvc.perform(get("/api/clases/10"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(10));
	}

	@Test
	void obtenerInexistenteDevuelve404() throws Exception {
		given(claseService.obtener(99L)).willThrow(new RecursoNoEncontradoException("No existe la clase con id 99"));

		mockMvc.perform(get("/api/clases/99"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("No existe la clase con id 99"));
	}

	@Test
	void actualizarDevuelve200() throws Exception {
		given(claseService.actualizar(eq(10L), any(ClaseRequest.class))).willReturn(respuesta());

		mockMvc.perform(put("/api/clases/10").contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(10));
	}

	@Test
	void actualizarInexistenteDevuelve404() throws Exception {
		willThrow(new RecursoNoEncontradoException("No existe la clase con id 99"))
				.given(claseService).actualizar(eq(99L), any(ClaseRequest.class));

		mockMvc.perform(put("/api/clases/99").contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
				.andExpect(status().isNotFound());
	}

	@Test
	void eliminarDevuelve204() throws Exception {
		doNothing().when(claseService).eliminar(10L);

		mockMvc.perform(delete("/api/clases/10")).andExpect(status().isNoContent());

		verify(claseService).eliminar(10L);
	}

	@Test
	void eliminarInexistenteDevuelve404() throws Exception {
		willThrow(new RecursoNoEncontradoException("No existe la clase con id 99"))
				.given(claseService).eliminar(99L);

		mockMvc.perform(delete("/api/clases/99")).andExpect(status().isNotFound());
	}

}
