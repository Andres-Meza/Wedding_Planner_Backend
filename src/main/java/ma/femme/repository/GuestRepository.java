package ma.femme.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ma.femme.entity.AttendanceStatus;
import ma.femme.entity.Guest;

public interface GuestRepository
    extends JpaRepository<Guest, Long>, JpaSpecificationExecutor<Guest> {

  // Invitado dueño de un enlace de la invitación divertida.
  Optional<Guest> findByFunToken(UUID funToken);

  // Todos los invitados con su invitación ya cargada, en el orden estable
  // (por invitación y luego por invitado), para la exportación a CSV.
  @Query("""
      select g from Guest g
        join fetch g.invitation i
      order by i.id, g.id
      """)
  List<Guest> findAllWithInvitationOrdered();

  // Invitados con esa respuesta que aún no tienen mesa, con su invitación ya
  // cargada (la asignación automática los agrupa por invitación).
  @Query("""
      select g from Guest g
        join fetch g.invitation
      where g.attendance = :attendance
        and not exists (select 1 from TableAssignment a where a.guest = g)
      order by g.id
      """)
  List<Guest> findWithoutTableByAttendance(@Param("attendance") AttendanceStatus attendance);

  // ¿Algún invitado tiene este menú elegido? (para bloquear borrados).
  boolean existsByMenuId(Long menuId);

  // Todos los invitados con su invitación y menú ya cargados, para el
  // reporte de alimentación (evita N+1 al armar cada fila).
  @Query("""
      select g from Guest g
        join fetch g.invitation
        left join fetch g.menu
      order by lower(g.fullName)
      """)
  List<Guest> findAllForFoodReport();
}
