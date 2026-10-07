package co.edu.javeriana.estudiante.repository;

import co.edu.javeriana.estudiante.model.Clase;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClaseRepository extends JpaRepository<Clase, Long> {

	List<Clase> findByUsuarioId(Long usuarioId);

	boolean existsByCodigo(String codigo);

	boolean existsByCodigoAndIdNot(String codigo, Long id);

	boolean existsByUsuarioId(Long usuarioId);

}
