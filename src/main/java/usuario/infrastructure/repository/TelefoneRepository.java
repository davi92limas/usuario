package usuario.infrastructure.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import usuario.infrastructure.entity.Telefone;

@Repository
public interface TelefoneRepository extends JpaRepository<Telefone, Long> {
}
