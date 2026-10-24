package ma.femme.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Respuesta de un invitado a una pregunta (una sola por pregunta, garantizado
// por la restricción única uq_fun_quiz_answer_question). Guarda solo las
// claves estables; los textos viven en FunQuizCatalog.
@Entity
@Table(name = "fun_quiz_answer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FunQuizAnswer {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "quiz_id", nullable = false, updatable = false)
  private FunQuiz quiz;

  @Column(name = "question_key", nullable = false, length = 40, updatable = false)
  private String questionKey;

  @Column(name = "option_key", nullable = false, length = 40)
  private String optionKey;

  @Column(name = "answered_at", nullable = false)
  private LocalDateTime answeredAt;
}
