package co.edu.javeriana.estudiante.service;

import co.edu.javeriana.estudiante.dto.UsuarioRequest;
import co.edu.javeriana.estudiante.dto.UsuarioResponse;
import co.edu.javeriana.estudiante.exception.RecursoDuplicadoException;
import co.edu.javeriana.estudiante.exception.RecursoEnUsoException;
import co.edu.javeriana.estudiante.exception.RecursoNoEncontradoException;
import co.edu.javeriana.estudiante.model.Usuario;
import co.edu.javeriana.estudiante.repository.ClaseRepository;
import co.edu.javeriana.estudiante.repository.UsuarioRepository;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Lógica de negocio del CRUD de usuarios. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsuarioService {

	private final UsuarioRepository usuarioRepository;

	private final ClaseRepository claseRepository;

	private final UsuarioMapper usuarioMapper;

	@Transactional
	public UsuarioResponse crear(UsuarioRequest request) {
		String correo = normalizarCorreo(request.getCorreo());
		if (usuarioRepository.existsByCorreo(correo)) {
			throw new RecursoDuplicadoException("Ya existe un usuario con el correo " + correo);
		}
		Usuario usuario = Usuario.builder()
				.nombre(request.getNombre().trim())
				.correo(correo)
				.build();
		return usuarioMapper.aResponse(usuarioRepository.save(usuario));
	}

	public List<UsuarioResponse> listar() {
		return usuarioRepository.findAll(Sort.by("nombre")).stream().map(usuarioMapper::aResponse).toList();
	}

	public UsuarioResponse obtener(Long id) {
		return usuarioMapper.aResponse(buscarUsuario(id));
	}

	/** Reemplazo completo: todos los campos del request sustituyen a los actuales. */
	@Transactional
	public UsuarioResponse actualizar(Long id, UsuarioRequest request) {
		Usuario usuario = buscarUsuario(id);
		String correo = normalizarCorreo(request.getCorreo());
		if (usuarioRepository.existsByCorreoAndIdNot(correo, id)) {
			throw new RecursoDuplicadoException("Ya existe un usuario con el correo " + correo);
		}
		usuario.setNombre(request.getNombre().trim());
		usuario.setCorreo(correo);
		return usuarioMapper.aResponse(usuarioRepository.save(usuario));
	}

	/** Una clase siempre pertenece a un usuario: no se elimina un usuario con clases. */
	@Transactional
	public void eliminar(Long id) {
		Usuario usuario = buscarUsuario(id);
		if (claseRepository.existsByUsuarioId(id)) {
			throw new RecursoEnUsoException("El usuario con id " + id + " tiene clases asociadas y no se puede eliminar");
		}
		usuarioRepository.delete(usuario);
	}

	private Usuario buscarUsuario(Long id) {
		return usuarioRepository.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("No existe el usuario con id " + id));
	}

	/** El correo es único sin distinguir mayúsculas: se guarda siempre en minúsculas. */
	private String normalizarCorreo(String correo) {
		return correo.trim().toLowerCase(Locale.ROOT);
	}

}
