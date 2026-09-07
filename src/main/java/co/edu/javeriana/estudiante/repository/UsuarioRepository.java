package co.edu.javeriana.estudiante.repository;

import co.edu.javeriana.estudiante.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
}
