package co.edu.javeriana.estudiante.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.javeriana.estudiante.controller.UsuarioController;
import co.edu.javeriana.estudiante.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UsuarioController.class)
class CorsConfigTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UsuarioService usuarioService;

	@Test
	void preflightDesdeElFrontendEstaPermitido() throws Exception {
		mockMvc.perform(options("/api/estudiante/usuarios")
						.header("Origin", "http://localhost:4200")
						.header("Access-Control-Request-Method", "POST"))
				.andExpect(status().isOk())
				.andExpect(header().string("Access-Control-Allow-Origin", "*"));
	}

	@Test
	void cualquierOrigenEstaPermitido() throws Exception {
		mockMvc.perform(get("/api/estudiante/usuarios").header("Origin", "http://otro-sitio.com"))
				.andExpect(status().isOk())
				.andExpect(header().string("Access-Control-Allow-Origin", "*"));
	}

}
