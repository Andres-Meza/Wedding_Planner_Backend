package ma.femme.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import ma.femme.dto.FunKeyCount;
import ma.femme.entity.FunQuiz;
import ma.femme.entity.FunQuizStatus;

public interface FunQuizRepository extends JpaRepository<FunQuiz, Long> {

  Optional<FunQuiz> findByGuestId(Long guestId);

  // Bloquea la fila del cuestionario hasta el fin de la transacción: dos
  // solicitudes simultáneas del mismo invitado (reintentos, doble clic)
  // se ejecutan una detrás de otra y no se pisan ni duplican respuestas.
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select q from FunQuiz q where q.guest.id = :guestId")
  Optional<FunQuiz> findByGuestIdForUpdate(@Param("guestId") Long guestId);

  // Crea el cuestionario del invitado si todavía no existe. ON CONFLICT lo
  // hace atómico: si dos solicitudes llegan a la vez, una inserta y la otra
  // no hace nada (y no falla), así que nunca hay dos cuestionarios.
  @Modifying
  @Query(value = """
      INSERT INTO fun_quiz (guest_id, status, started_at, updated_at)
      VALUES (:guestId, 'IN_PROGRESS', :now, :now)
      ON CONFLICT (guest_id) DO NOTHING
      """, nativeQuery = true)
  int insertIfAbsent(@Param("guestId") Long guestId, @Param("now") LocalDateTime now);

  List<FunQuiz> findByGuestIdIn(Collection<Long> guestIds);

  long countByStatus(FunQuizStatus status);

  // Cuántos cuestionarios completados hay por veredicto.
  @Query("""
      select new ma.femme.dto.FunKeyCount(q.resultKey, count(q))
      from FunQuiz q
      where q.status = ma.femme.entity.FunQuizStatus.COMPLETED
      group by q.resultKey
      """)
  List<FunKeyCount> countCompletedByResult();
}
