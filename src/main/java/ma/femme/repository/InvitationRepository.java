package ma.femme.repository;

import ma.femme.entity.Invitation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {

    @EntityGraph(attributePaths = "guests")
    Optional<Invitation> findByUniqueToken(UUID uniqueToken);
}