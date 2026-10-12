package ma.femme.repository;

import ma.femme.entity.TableAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TableAssignmentRepository extends JpaRepository<TableAssignment, Long> {

    List<TableAssignment> findByTableWeddingId(Long tableWeddingId);

    long countByTableWeddingId(Long tableWeddingId);

    boolean existsByGuestId(Long guestId);

    Optional<TableAssignment> findByGuestId(Long guestId);

    void deleteByGuestId(Long guestId);

    // ¿Esa silla ya la ocupa otro invitado?
    boolean existsByTableWeddingIdAndSeatIndexAndGuestIdNot(
            Long tableWeddingId, Integer seatIndex, Long guestId);

    // ¿Hay invitados sentados en sillas que desaparecerían al reducir la mesa?
    boolean existsByTableWeddingIdAndSeatIndexGreaterThanEqual(
            Long tableWeddingId, Integer seatIndex);
}
