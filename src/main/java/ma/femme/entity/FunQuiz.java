package ma.femme.entity;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Cuestionario de la invitación divertida de UN invitado. La fila se crea al
// guardar la primera respuesta; sus respuestas viven en FunQuizAnswer.
@Entity
@Table(name = "fun_quiz")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FunQuiz {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "guest_id", nullable = false, unique = true, updatable = false)
  private Guest guest;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  @Builder.Default
  private FunQuizStatus status = FunQuizStatus.IN_PROGRESS;

  @Column(name = "started_at", nullable = false)
  private LocalDateTime startedAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @Column(name = "completed_at")
  private LocalDateTime completedAt;

  // Clave del veredicto (FunQuizCatalog.Verdict). null mientras no se complete.
  @Column(name = "result_key", length = 40)
  private String resultKey;

  public boolean isCompleted() {
    return status == FunQuizStatus.COMPLETED;
  }

  public void markCompleted(String verdictKey, LocalDateTime now) {
    this.status = FunQuizStatus.COMPLETED;
    this.resultKey = verdictKey;
    this.completedAt = now;
    this.updatedAt = now;
  }

  // Las columnas son TIMESTAMP sin zona y se escriben con la hora del
  // servidor; se exponen como Instant (igual que el mensaje para los novios)
  // para que el navegador las muestre en la hora local de quien las ve.
  public static Instant toInstant(LocalDateTime value) {
    return value == null ? null : value.atZone(ZoneId.systemDefault()).toInstant();
  }
}
