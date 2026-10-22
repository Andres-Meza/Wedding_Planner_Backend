package ma.femme.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import ma.femme.entity.TableAssignment;

public interface TableAssignmentRepository extends JpaRepository<TableAssignment, Long> {

  // Lugares ocupados de una mesa agrupados en una sola consulta.
  interface TableOccupancy {
    Long getTableId();

    Long getOccupiedSpots();
  }

  long countByTableWeddingId(Long tableWeddingId);

  @Query("""
      select a.tableWedding.id as tableId, count(a) as occupiedSpots
      from TableAssignment a
      group by a.tableWedding.id
      """)
  List<TableOccupancy> countOccupiedSpotsByTable();

  // Mesas sin ninguna asignación no aparecen en el mapa (ocupación 0).
  default Map<Long, Long> occupiedSpotsByTable() {
    return countOccupiedSpotsByTable().stream()
        .collect(Collectors.toMap(TableOccupancy::getTableId, TableOccupancy::getOccupiedSpots));
  }

  // Todas las asignaciones con su invitado y su mesa ya cargados.
  @Query("""
      select a from TableAssignment a
        join fetch a.guest
        join fetch a.tableWedding
      order by a.id
      """)
  List<TableAssignment> findAllWithGuestAndTable();

  Optional<TableAssignment> findByGuestId(Long guestId);

  void deleteByGuestId(Long guestId);

  // ¿Esa silla ya la ocupa otro invitado?
  boolean existsByTableWeddingIdAndSeatIndexAndGuestIdNot(
      Long tableWeddingId, Integer seatIndex, Long guestId);

  // ¿Hay invitados sentados en sillas que desaparecerían al reducir la mesa?
  boolean existsByTableWeddingIdAndSeatIndexGreaterThanEqual(
      Long tableWeddingId, Integer seatIndex);
}
