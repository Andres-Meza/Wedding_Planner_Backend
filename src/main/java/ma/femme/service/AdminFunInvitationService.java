package ma.femme.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.RequiredArgsConstructor;
import ma.femme.dto.AdminFunAnswerDTO;
import ma.femme.dto.AdminFunGuestDTO;
import ma.femme.dto.AdminFunGuestDetailDTO;
import ma.femme.dto.AdminFunPageDTO;
import ma.femme.dto.AdminFunStatsDTO;
import ma.femme.dto.FunAnswerCount;
import ma.femme.dto.FunKeyCount;
import ma.femme.dto.FunQuizState;
import ma.femme.dto.FunVerdictDTO;
import ma.femme.entity.FunQuiz;
import ma.femme.entity.FunQuizAnswer;
import ma.femme.entity.FunQuizStatus;
import ma.femme.entity.Guest;
import ma.femme.entity.Invitation;
import ma.femme.exception.GuestNotFoundException;
import ma.femme.export.FunQuizCsvExporter;
import ma.femme.repository.FunQuizAnswerRepository;
import ma.femme.repository.FunQuizRepository;
import ma.femme.repository.GuestRepository;

// Administración de las dos invitaciones (formal y divertida) por invitado.
// Solo se llama desde /admin/** (protegido con rol ADMIN).
@Service
@RequiredArgsConstructor
public class AdminFunInvitationService {

  static final int DEFAULT_PAGE_SIZE = 10;
  static final int MAX_PAGE_SIZE = 50;

  private final GuestRepository guestRepository;
  private final FunQuizRepository quizRepository;
  private final FunQuizAnswerRepository answerRepository;

  @Transactional(readOnly = true)
  public AdminFunPageDTO list(
      int page,
      int size,
      String search,
      Long invitationId,
      FunQuizState state) {

    Pageable pageable = PageRequest.of(
        Math.max(page, 0),
        clampSize(size),
        // Por invitación y luego por invitado: las familias quedan juntas.
        Sort.by("invitation.id", "id"));

    Page<Guest> result = guestRepository.findAll(
        buildSpecification(search, invitationId, state), pageable);

    List<AdminFunGuestDTO> content = toAdminDtos(result.getContent());

    return new AdminFunPageDTO(
        content,
        result.getNumber(),
        result.getSize(),
        result.getTotalElements(),
        result.getTotalPages());
  }

  @Transactional(readOnly = true)
  public AdminFunGuestDetailDTO getDetail(Long guestId) {
    Guest guest = findGuestOrThrow(guestId);
    return toDetail(guest, quizRepository.findByGuestId(guestId).orElse(null));
  }

  @Transactional(readOnly = true)
  public AdminFunStatsDTO getStats() {
    long total = guestRepository.count();
    long inProgress = quizRepository.countByStatus(FunQuizStatus.IN_PROGRESS);
    long completed = quizRepository.countByStatus(FunQuizStatus.COMPLETED);
    // Derivado de los otros dos: los tres estados son excluyentes y suman el total.
    long notStarted = Math.max(total - inProgress - completed, 0);

    double completionPercent = total == 0
        ? 0.0
        : Math.round(completed * 1000.0 / total) / 10.0;

    Map<String, Long> countByAnswer = new HashMap<>();
    for (FunAnswerCount row : answerRepository.countByQuestionAndOption()) {
      countByAnswer.put(row.questionKey() + "|" + row.optionKey(), row.total());
    }

    List<AdminFunStatsDTO.QuestionStat> questions = FunQuizCatalog.QUESTIONS.stream()
        .map(question -> {
          List<AdminFunStatsDTO.OptionStat> options = question.options().stream()
              .map(option -> new AdminFunStatsDTO.OptionStat(
                  option.key(),
                  option.label(),
                  countByAnswer.getOrDefault(question.key() + "|" + option.key(), 0L)))
              .toList();
          long answers = options.stream().mapToLong(AdminFunStatsDTO.OptionStat::count).sum();
          return new AdminFunStatsDTO.QuestionStat(
              question.key(), question.title(), answers, options);
        })
        .toList();

    Map<String, Long> countByVerdict = quizRepository.countCompletedByResult().stream()
        .collect(Collectors.toMap(FunKeyCount::key, FunKeyCount::total, Long::sum));

    List<AdminFunStatsDTO.VerdictStat> verdicts = FunQuizCatalog.VERDICTS.stream()
        .map(verdict -> new AdminFunStatsDTO.VerdictStat(
            verdict.key(), verdict.headline(), countByVerdict.getOrDefault(verdict.key(), 0L)))
        .sorted(Comparator.comparingLong(AdminFunStatsDTO.VerdictStat::count).reversed())
        .toList();

    return new AdminFunStatsDTO(
        total, notStarted, inProgress, completed, completionPercent, questions, verdicts);
  }

  // Genera un token NUEVO para la invitación divertida de un invitado. Invalida
  // el enlace anterior (si ya se envió, deja de abrir), por eso el controlador
  // exige confirmación explícita. Las respuestas y el resultado se conservan:
  // la identidad del invitado no cambia, solo su credencial.
  @Transactional
  public AdminFunGuestDTO regenerateToken(Long guestId) {
    Guest guest = findGuestOrThrow(guestId);
    guest.setFunToken(UUID.randomUUID());

    return toAdminDtos(List.of(guest)).get(0);
  }

  @Transactional(readOnly = true)
  public byte[] exportCsv() {
    List<Guest> guests = guestRepository.findAllWithInvitationOrdered();

    Map<Long, FunQuiz> quizzesByGuest = quizRepository.findAll().stream()
        .collect(Collectors.toMap(quiz -> quiz.getGuest().getId(), Function.identity()));
    Map<Long, List<FunQuizAnswer>> answersByQuiz = answerRepository.findAll().stream()
        .collect(Collectors.groupingBy(answer -> answer.getQuiz().getId()));

    List<AdminFunGuestDetailDTO> rows = guests.stream()
        .map(guest -> {
          FunQuiz quiz = quizzesByGuest.get(guest.getId());
          List<FunQuizAnswer> answers = quiz == null
              ? List.of()
              : answersByQuiz.getOrDefault(quiz.getId(), List.of());
          return toDetail(guest, quiz, answers);
        })
        .toList();

    return FunQuizCsvExporter.write(rows, FunQuizCatalog.QUESTIONS);
  }

  private Guest findGuestOrThrow(Long guestId) {
    return guestRepository.findById(guestId)
        .orElseThrow(() -> new GuestNotFoundException(guestId));
  }

  private int clampSize(int size) {
    if (size < 1) {
      return DEFAULT_PAGE_SIZE;
    }
    return Math.min(size, MAX_PAGE_SIZE);
  }

  // Filtros combinables: búsqueda por nombre (del invitado o de su
  // persona/familia), invitación concreta y estado del cuestionario.
  private Specification<Guest> buildSpecification(
      String search,
      Long invitationId,
      FunQuizState state) {

    String pattern = likePattern(search);

    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();

      // Carga la invitación en la misma consulta (no en la de conteo).
      if (query.getResultType() != Long.class && query.getResultType() != long.class) {
        root.fetch("invitation");
      }

      if (invitationId != null) {
        predicates.add(cb.equal(root.get("invitation").get("id"), invitationId));
      }

      if (pattern != null) {
        predicates.add(cb.or(
            cb.like(cb.lower(root.get("fullName")), pattern, '\\'),
            cb.like(cb.lower(root.get("invitation").get("referenceName")), pattern, '\\')));
      }

      if (state != null) {
        Subquery<Long> quizOfGuest = query.subquery(Long.class);
        Root<FunQuiz> quiz = quizOfGuest.from(FunQuiz.class);
        Predicate sameGuest = cb.equal(quiz.get("guest"), root);

        switch (state) {
          case NOT_STARTED -> {
            quizOfGuest.select(quiz.<Long>get("id")).where(sameGuest);
            predicates.add(cb.not(cb.exists(quizOfGuest)));
          }
          case IN_PROGRESS -> {
            quizOfGuest.select(quiz.<Long>get("id")).where(
                sameGuest, cb.equal(quiz.get("status"), FunQuizStatus.IN_PROGRESS));
            predicates.add(cb.exists(quizOfGuest));
          }
          case COMPLETED -> {
            quizOfGuest.select(quiz.<Long>get("id")).where(
                sameGuest, cb.equal(quiz.get("status"), FunQuizStatus.COMPLETED));
            predicates.add(cb.exists(quizOfGuest));
          }
        }
      }

      return cb.and(predicates.toArray(Predicate[]::new));
    };
  }

  // Patrón LIKE "contiene" en minúsculas; escapa % _ y \ para que lo que
  // escriba el admin se busque literalmente.
  private String likePattern(String search) {
    if (search == null || search.isBlank()) {
      return null;
    }
    String escaped = search.strip().toLowerCase(Locale.ROOT)
        .replace("\\", "\\\\")
        .replace("%", "\\%")
        .replace("_", "\\_");
    return "%" + escaped + "%";
  }

  // Arma las filas de una página con DOS consultas extra (cuestionarios y
  // respuestas de toda la página), no una por invitado.
  private List<AdminFunGuestDTO> toAdminDtos(List<Guest> guests) {
    if (guests.isEmpty()) {
      return List.of();
    }

    List<Long> guestIds = guests.stream().map(Guest::getId).toList();
    Map<Long, FunQuiz> quizzesByGuest = quizRepository.findByGuestIdIn(guestIds).stream()
        .collect(Collectors.toMap(quiz -> quiz.getGuest().getId(), Function.identity()));

    List<Long> quizIds = quizzesByGuest.values().stream().map(FunQuiz::getId).toList();
    Map<Long, Long> answeredByQuiz = quizIds.isEmpty()
        ? Map.of()
        : answerRepository.findByQuizIdIn(quizIds).stream()
            .collect(Collectors.groupingBy(answer -> answer.getQuiz().getId(), Collectors.counting()));

    return guests.stream()
        .map(guest -> {
          FunQuiz quiz = quizzesByGuest.get(guest.getId());
          int answered = quiz == null ? 0 : answeredByQuiz.getOrDefault(quiz.getId(), 0L).intValue();
          return toAdminDto(guest, quiz, answered);
        })
        .toList();
  }

  private AdminFunGuestDTO toAdminDto(Guest guest, FunQuiz quiz, int answeredCount) {
    Invitation invitation = guest.getInvitation();

    FunQuizState state = quiz == null
        ? FunQuizState.NOT_STARTED
        : quiz.isCompleted() ? FunQuizState.COMPLETED : FunQuizState.IN_PROGRESS;

    String verdictKey = quiz != null && quiz.isCompleted() ? quiz.getResultKey() : null;
    String verdictHeadline = verdictKey == null
        ? null
        : FunQuizCatalog.findVerdict(verdictKey).map(FunQuizCatalog.Verdict::headline).orElse(verdictKey);

    return new AdminFunGuestDTO(
        guest.getId(),
        guest.getFullName(),
        invitation.getId(),
        invitation.getReferenceName(),
        invitation.getType(),
        invitation.getUniqueToken(),
        guest.getFunToken(),
        guest.getAttendance(),
        state,
        answeredCount,
        FunQuizCatalog.QUESTIONS.size(),
        quiz == null ? null : FunQuiz.toInstant(quiz.getStartedAt()),
        quiz == null ? null : FunQuiz.toInstant(quiz.getUpdatedAt()),
        quiz == null ? null : FunQuiz.toInstant(quiz.getCompletedAt()),
        verdictKey,
        verdictHeadline);
  }

  private AdminFunGuestDetailDTO toDetail(Guest guest, FunQuiz quiz) {
    List<FunQuizAnswer> answers = quiz == null
        ? List.of()
        : answerRepository.findByQuizId(quiz.getId());
    return toDetail(guest, quiz, answers);
  }

  private AdminFunGuestDetailDTO toDetail(Guest guest, FunQuiz quiz, List<FunQuizAnswer> answers) {
    Map<String, FunQuizAnswer> byQuestion = answers.stream()
        .collect(Collectors.toMap(FunQuizAnswer::getQuestionKey, Function.identity()));

    // En el orden del catálogo; una opción ya retirada del catálogo se
    // muestra con su clave en vez de omitirse.
    List<AdminFunAnswerDTO> resolved = FunQuizCatalog.QUESTIONS.stream()
        .filter(question -> byQuestion.containsKey(question.key()))
        .map(question -> {
          FunQuizAnswer answer = byQuestion.get(question.key());
          String optionLabel = question.findOption(answer.getOptionKey())
              .map(FunQuizCatalog.Option::label)
              .orElse(answer.getOptionKey());
          return new AdminFunAnswerDTO(
              question.key(),
              question.title(),
              answer.getOptionKey(),
              optionLabel,
              FunQuiz.toInstant(answer.getAnsweredAt()));
        })
        .toList();

    FunVerdictDTO verdict = quiz != null && quiz.isCompleted()
        ? FunQuizCatalog.findVerdict(quiz.getResultKey()).map(FunVerdictDTO::fromCatalog).orElse(null)
        : null;

    return new AdminFunGuestDetailDTO(toAdminDto(guest, quiz, resolved.size()), resolved, verdict);
  }
}
