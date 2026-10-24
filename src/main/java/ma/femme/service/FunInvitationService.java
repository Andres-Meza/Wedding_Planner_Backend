package ma.femme.service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ma.femme.dto.FunInvitationDTO;
import ma.femme.dto.FunQuestionDTO;
import ma.femme.dto.FunQuizState;
import ma.femme.dto.FunVerdictDTO;
import ma.femme.entity.FunQuiz;
import ma.femme.entity.FunQuizAnswer;
import ma.femme.entity.Guest;
import ma.femme.exception.FunInvitationNotFoundException;
import ma.femme.exception.FunQuizCompletedException;
import ma.femme.repository.FunQuizAnswerRepository;
import ma.femme.repository.FunQuizRepository;
import ma.femme.repository.GuestRepository;

// Invitación divertida vista por el invitado. Todas las operaciones parten
// del token individual: de él sale el invitado, y SOLO se lee o escribe el
// cuestionario de ese invitado (nunca se recibe un id de invitado desde fuera).
@Service
@RequiredArgsConstructor
public class FunInvitationService {

  private final GuestRepository guestRepository;
  private final FunQuizRepository quizRepository;
  private final FunQuizAnswerRepository answerRepository;

  @Transactional(readOnly = true)
  public FunInvitationDTO getByToken(UUID token) {
    Guest guest = findGuestOrThrow(token);

    return quizRepository.findByGuestId(guest.getId())
        .map(quiz -> toDto(guest, quiz))
        .orElseGet(() -> toDto(guest, null));
  }

  // Guarda (o reemplaza) la respuesta a UNA pregunta. Idempotente: repetir la
  // misma solicitud deja el mismo estado y nunca crea una segunda respuesta.
  // Una vez completado el cuestionario ya no se puede cambiar.
  @Transactional
  public FunInvitationDTO saveAnswer(UUID token, String questionKey, String optionKey) {
    Guest guest = findGuestOrThrow(token);

    FunQuizCatalog.Question question = FunQuizCatalog.findQuestion(questionKey)
        .orElseThrow(() -> new IllegalArgumentException("La pregunta no existe"));
    if (question.findOption(optionKey).isEmpty()) {
      throw new IllegalArgumentException("La opción no es válida para esta pregunta");
    }

    // Crea el cuestionario con la primera respuesta (atómico) y lo bloquea
    // para que respuestas simultáneas del mismo invitado se serialicen.
    LocalDateTime now = LocalDateTime.now();
    quizRepository.insertIfAbsent(guest.getId(), now);
    FunQuiz quiz = quizRepository.findByGuestIdForUpdate(guest.getId())
        .orElseThrow(() -> new IllegalStateException("The quiz could not be created"));

    if (quiz.isCompleted()) {
      throw new FunQuizCompletedException();
    }

    FunQuizAnswer existing = answerRepository
        .findByQuizIdAndQuestionKey(quiz.getId(), questionKey)
        .orElse(null);

    if (existing == null) {
      answerRepository.save(FunQuizAnswer.builder()
          .quiz(quiz)
          .questionKey(questionKey)
          .optionKey(optionKey)
          .answeredAt(now)
          .build());
      quiz.setUpdatedAt(now);
    } else if (!existing.getOptionKey().equals(optionKey)) {
      existing.setOptionKey(optionKey);
      existing.setAnsweredAt(now);
      quiz.setUpdatedAt(now);
    }
    // Misma respuesta de nuevo (reintento): no se toca nada.

    return toDto(guest, quiz);
  }

  // Cierra el cuestionario y guarda el veredicto. Exige que TODAS las
  // preguntas del catálogo tengan respuesta guardada en el servidor. Si ya
  // estaba completado devuelve el mismo resultado (idempotente): el veredicto
  // se calcula una sola vez y queda persistido.
  @Transactional
  public FunInvitationDTO complete(UUID token) {
    Guest guest = findGuestOrThrow(token);

    FunQuiz quiz = quizRepository.findByGuestIdForUpdate(guest.getId())
        .orElseThrow(() -> new IllegalArgumentException("Faltan preguntas por responder"));

    if (quiz.isCompleted()) {
      return toDto(guest, quiz);
    }

    Map<String, String> answers = answersByQuestion(quiz);
    boolean allAnswered = FunQuizCatalog.QUESTIONS.stream()
        .allMatch(question -> answers.containsKey(question.key()));
    if (!allAnswered) {
      throw new IllegalArgumentException("Faltan preguntas por responder");
    }

    quiz.markCompleted(FunQuizResolver.resolve(answers), LocalDateTime.now());

    return toDto(guest, quiz);
  }

  private Guest findGuestOrThrow(UUID token) {
    return guestRepository.findByFunToken(token)
        .orElseThrow(FunInvitationNotFoundException::new);
  }

  // Clave de pregunta -> clave de opción, en el orden del catálogo.
  private Map<String, String> answersByQuestion(FunQuiz quiz) {
    Map<String, String> stored = new LinkedHashMap<>();
    for (FunQuizAnswer answer : answerRepository.findByQuizId(quiz.getId())) {
      stored.put(answer.getQuestionKey(), answer.getOptionKey());
    }

    Map<String, String> ordered = new LinkedHashMap<>();
    for (FunQuizCatalog.Question question : FunQuizCatalog.QUESTIONS) {
      String optionKey = stored.get(question.key());
      if (optionKey != null) {
        ordered.put(question.key(), optionKey);
      }
    }
    return ordered;
  }

  private FunInvitationDTO toDto(Guest guest, FunQuiz quiz) {
    List<FunQuestionDTO> questions = FunQuizCatalog.QUESTIONS.stream()
        .map(FunQuestionDTO::fromCatalog)
        .toList();

    if (quiz == null) {
      return new FunInvitationDTO(
          guest.getFullName(), FunQuizState.NOT_STARTED, questions, Map.of(), null, null);
    }

    FunVerdictDTO verdict = quiz.isCompleted() ? verdictFor(quiz) : null;

    return new FunInvitationDTO(
        guest.getFullName(),
        quiz.isCompleted() ? FunQuizState.COMPLETED : FunQuizState.IN_PROGRESS,
        questions,
        answersByQuestion(quiz),
        verdict,
        FunQuiz.toInstant(quiz.getCompletedAt()));
  }

  // Si algún día se retira un veredicto del catálogo, los cuestionarios que
  // lo tenían guardado muestran el general en vez de fallar.
  private FunVerdictDTO verdictFor(FunQuiz quiz) {
    FunQuizCatalog.Verdict verdict = FunQuizCatalog.findVerdict(quiz.getResultKey())
        .or(() -> FunQuizCatalog.findVerdict(FunQuizCatalog.V_GENERAL))
        .orElseThrow(() -> new IllegalStateException("The general verdict is missing"));
    return FunVerdictDTO.fromCatalog(verdict);
  }
}
