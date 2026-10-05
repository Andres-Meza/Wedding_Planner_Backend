package ma.femme.repository;

import ma.femme.entity.TableAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TableAssignmentRepository extends JpaRepository<TableAssignment, Long> {

    List<TableAssignment> findByTableWeddingId(Long tableWeddingId);

    long countByTableWeddingId(Long tableWeddingId);

    boolean existsByGuestId(Long guestId);

    void deleteByGuestId(Long guestId);
}
