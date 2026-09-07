package co.edu.javeriana.estudiante.service;

import co.edu.javeriana.estudiante.dto.ClaseRequest;
import co.edu.javeriana.estudiante.dto.ClaseResponse;
import co.edu.javeriana.estudiante.exception.RecursoDuplicadoException;
import co.edu.javeriana.estudiante.exception.RecursoNoEncontradoException;
import co.edu.javeriana.estudiante.model.Clase;
import co.edu.javeriana.estudiante.model.Usuario;
import co.edu.javeriana.estudiante.repository.ClaseRepository;
import co.edu.javeriana.estudiante.repository.UsuarioRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Lógica de negocio del CRUD de clases. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClaseService {

	private final ClaseRepository claseRepository;

	private final UsuarioRepository usuarioRepository;

	private final ClaseMapper claseMapper;

	@Transactional
	public ClaseResponse crear(ClaseRequest request) {
		if (claseRepository.existsByCodigo(request.getCodigo())) {
			throw new RecursoDuplicadoException("Ya existe una clase con el código " + request.getCodigo());
		}
		Usuario usuario = buscarUsuario(request.getUsuarioId());
		Clase clase = Clase.builder()
				.codigo(request.getCodigo())
				.nombre(request.getNombre())
				.creditos(request.getCreditos())
				.semestre(request.getSemestre())
				.usuario(usuario)
				.build();
		return claseMapper.aResponse(claseRepository.save(clase));
	}

	public List<ClaseResponse> listar() {
		return claseRepository.findAll().stream().map(claseMapper::aResponse).toList();
	}

	public List<ClaseResponse> listarPorUsuario(Long usuarioId) {
		if (!usuarioRepository.existsById(usuarioId)) {
			throw new RecursoNoEncontradoException("No existe el usuario con id " + usuarioId);
		}
		return claseRepository.findByUsuarioId(usuarioId).stream().map(claseMapper::aResponse).toList();
	}

	public ClaseResponse obtener(Long id) {
		return claseMapper.aResponse(buscarClase(id));
	}

	/** Reemplazo completo: todos los campos del request sustituyen a los actuales. */
	@Transactional
	public ClaseResponse actualizar(Long id, ClaseRequest request) {
		Clase clase = buscarClase(id);
		if (claseRepository.existsByCodigoAndIdNot(request.getCodigo(), id)) {
			throw new RecursoDuplicadoException("Ya existe una clase con el código " + request.getCodigo());
		}
		Usuario usuario = buscarUsuario(request.getUsuarioId());
		clase.setCodigo(request.getCodigo());
		clase.setNombre(request.getNombre());
		clase.setCreditos(request.getCreditos());
		clase.setSemestre(request.getSemestre());
		clase.setUsuario(usuario);
		return claseMapper.aResponse(claseRepository.save(clase));
	}

	@Transactional
	public void eliminar(Long id) {
		claseRepository.delete(buscarClase(id));
	}

	private Clase buscarClase(Long id) {
		return claseRepository.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("No existe la clase con id " + id));
	}

	private Usuario buscarUsuario(Long usuarioId) {
		return usuarioRepository.findById(usuarioId)
				.orElseThrow(() -> new RecursoNoEncontradoException("No existe el usuario con id " + usuarioId));
	}

}
