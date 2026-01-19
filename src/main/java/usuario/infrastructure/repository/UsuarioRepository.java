package usuario.infrastructure.repository;


import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import usuario.infrastructure.entity.Usuario;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
  
  boolean existsByEmail(String email);
  
  Optional<Usuario> findByEmail(String email);
  
  @Transactional
  void deleteByEmail(String email);
}
