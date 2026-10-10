package ma.femme.repository;

import ma.femme.entity.Invitation;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {

  // Carga también el menú de cada invitado para no hacer una consulta extra
  // por cada uno al armar la respuesta.
  @EntityGraph(attributePaths = {"guests", "guests.menu"})
  Optional<Invitation> findByUniqueToken(UUID uniqueToken);
}
