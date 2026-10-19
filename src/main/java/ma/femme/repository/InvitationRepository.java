package ma.femme.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import ma.femme.entity.Invitation;

public interface InvitationRepository
    extends JpaRepository<Invitation, Long>, JpaSpecificationExecutor<Invitation> {

  // Invitaciones que ya dejaron un mensaje para los novios.
  long countByCoupleMessageIsNotNull();

  // Carga también el menú de cada invitado para no hacer una consulta extra
  // por cada uno al armar la respuesta.
  @EntityGraph(attributePaths = { "guests", "guests.menu" })
  Optional<Invitation> findByUniqueToken(UUID uniqueToken);
}
