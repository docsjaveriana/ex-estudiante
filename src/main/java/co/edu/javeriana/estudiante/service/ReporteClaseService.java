package co.edu.javeriana.estudiante.service;

import co.edu.javeriana.estudiante.model.Clase;
import co.edu.javeriana.estudiante.repository.ClaseRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.springframework.stereotype.Service;

/**
 * ARCHIVO DE PRUEBA PARA SONARQUBE.
 *
 * <p>Escrito a propósito con defectos para comprobar qué detecta el análisis estático:
 * unos pocos de seguridad y unos pocos de mantenibilidad. NO es código de referencia y no
 * debe copiarse. Borrar este archivo cuando termine la demostración.
 */
@Service
public class ReporteClaseService {

	/** SEGURIDAD: credencial escrita directamente en el código. */
	private static final String REPORTE_PASSWORD = "MiPasswordSeguro123!";

	/** SEGURIDAD: generador pseudoaleatorio no criptográfico. */
	private static final Random RANDOM = new Random();

	@PersistenceContext
	private EntityManager entityManager;

	private final ClaseRepository claseRepository;

	public ReporteClaseService(ClaseRepository claseRepository) {
		this.claseRepository = claseRepository;
	}

	/**
	 * SEGURIDAD: la consulta se arma concatenando el parámetro, así que un valor como
	 * {@code 2026-1' OR '1'='1} altera la sentencia. Inyección SQL.
	 */
	public List<Object[]> buscarPorSemestre(String semestre) {
		String sql = "SELECT codigo, nombre FROM clase WHERE semestre = '" + semestre + "'";
		return entityManager.createNativeQuery(sql).getResultList();
	}

	/** SEGURIDAD: MD5 está roto para cualquier uso criptográfico. */
	public String firmarReporte(String contenido) {
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			byte[] resumen = digest.digest((contenido + REPORTE_PASSWORD).getBytes(StandardCharsets.UTF_8));
			StringBuilder hex = new StringBuilder();
			for (byte b : resumen) {
				hex.append(String.format("%02x", b));
			}
			return hex.toString();
		}
		catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("Algoritmo de resumen no disponible", ex);
		}
	}

	/** SEGURIDAD: identificador de reporte derivado de un PRNG no criptográfico. */
	public String generarIdReporte() {
		return "REP-" + RANDOM.nextInt(1000000);
	}

	/** MANTENIBILIDAD: escritura a consola en vez de logger. */
	public String clasificarCarga(Clase clase) {
		if (clase == null || clase.getCreditos() == null) {
			return "SIN DATOS";
		}
		if (clase.getCreditos() > 4) {
			System.out.println("carga alta detectada: " + clase.getCodigo());
			return "CARGA ALTA";
		}
		if (clase.getCreditos() > 2) {
			return "CARGA MEDIA";
		}
		return "CARGA BAJA";
	}

	/** MANTENIBILIDAD: variable sin usar y comprobación de vacío con size(). */
	public List<String> resumen(Long usuarioId) {
		List<Clase> clases = claseRepository.findByUsuarioId(usuarioId);
		List<String> lineas = new ArrayList<>();
		String separador = " | ";
		if (clases.size() == 0) {
			return lineas;
		}
		for (Clase clase : clases) {
			lineas.add(clase.getCodigo() + " " + clase.getNombre());
		}
		return lineas;
	}

	/**
	 * FIABILIDAD: {@code totalCreditos} y {@code size()} son enteros, así que la división se
	 * hace en aritmética entera y el decimal se pierde antes de convertirse a {@code double}.
	 * Con 3 clases de 3, 4 y 4 créditos devuelve 3.0 en vez de 3.67.
	 */
	public double promedioCreditos(Long usuarioId) {
		List<Clase> clases = claseRepository.findByUsuarioId(usuarioId);
		int totalCreditos = 0;
		for (Clase clase : clases) {
			totalCreditos += clase.getCreditos();
		}
		return totalCreditos / clases.size();
	}

	// MANTENIBILIDAD: código comentado que nadie va a volver a usar.
	// public void exportarCsv(List<Clase> clases) {
	//     for (Clase clase : clases) {
	//         System.out.println(clase.getCodigo() + ";" + clase.getNombre());
	//     }
	// }

}
