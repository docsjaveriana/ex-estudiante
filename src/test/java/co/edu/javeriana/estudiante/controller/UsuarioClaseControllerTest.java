package co.edu.javeriana.estudiante.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.javeriana.estudiante.dto.ClaseResponse;
import co.edu.javeriana.estudiante.exception.RecursoNoEncontradoException;
import co.edu.javeriana.estudiante.service.ClaseService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UsuarioClaseController.class)
class UsuarioClaseControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ClaseService claseService;

	@Test
	void listarPorUsuarioDevuelve200() throws Exception {
		ClaseResponse clase = ClaseResponse.builder()
				.id(10L)
				.codigo("ISIS-1101")
				.nombre("Algoritmos")
				.creditos(3)
				.usuarioId(1L)
				.build();
		given(claseService.listarPorUsuario(1L)).willReturn(List.of(clase));

		mockMvc.perform(get("/api/estudiante/usuarios/1/clases"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].usuarioId").value(1));
	}

	@Test
	void listarPorUsuarioInexistenteDevuelve404() throws Exception {
		given(claseService.listarPorUsuario(99L))
				.willThrow(new RecursoNoEncontradoException("No existe el usuario con id 99"));

		mockMvc.perform(get("/api/estudiante/usuarios/99/clases")).andExpect(status().isNotFound());
	}

}
