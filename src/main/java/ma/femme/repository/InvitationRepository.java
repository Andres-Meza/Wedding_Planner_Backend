package ma.femme.repository;

import ma.femme.entity.Invitation;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {

    @EntityGraph(attributePaths = "guests")
    Optional<Invitation> findByUniqueToken(UUID uniqueToken);
}
