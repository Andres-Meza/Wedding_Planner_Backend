package ma.femme.repository;

import ma.femme.entity.AttendanceStatus;
import ma.femme.entity.Guest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GuestRepository extends JpaRepository<Guest, Long> {

  List<Guest> findByInvitationId(Long invitationId);

  // El nombre debe coincidir con el campo de la entidad ("attendance"),
  // no con el tipo del enum ("attendanceStatus" no existe como propiedad).
  List<Guest> findByAttendance(AttendanceStatus attendance);

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
