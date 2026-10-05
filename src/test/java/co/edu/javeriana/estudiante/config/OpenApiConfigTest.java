package co.edu.javeriana.estudiante.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/** La documentación debe quedar bajo /api/estudiante, el único prefijo que enruta el Ingress. */
@SpringBootTest
@AutoConfigureMockMvc
class OpenApiConfigTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void apiDocsPublicaLosEndpointsBajoElPrefijo() throws Exception {
		mockMvc.perform(get("/api/estudiante/v3/api-docs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.info.title").value("API Estudiante"))
				.andExpect(jsonPath("$.paths['/api/estudiante/clases']").exists())
				.andExpect(jsonPath("$.paths['/api/estudiante/clases/{id}']").exists())
				.andExpect(jsonPath("$.paths['/api/estudiante/usuarios/{usuarioId}/clases']").exists());
	}

	@Test
	void swaggerUiRedirigeAlIndiceBajoElPrefijo() throws Exception {
		mockMvc.perform(get("/api/estudiante/swagger-ui.html"))
				.andExpect(status().is3xxRedirection());
	}

	@Test
	void swaggerUiSirveSusRecursosBajoElPrefijo() throws Exception {
		mockMvc.perform(get("/api/estudiante/swagger-ui/index.html"))
				.andExpect(status().isOk());
	}

}
