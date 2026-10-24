package ma.femme.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ma.femme.dto.AdminFunGuestDTO;
import ma.femme.dto.AdminFunGuestDetailDTO;
import ma.femme.dto.AdminFunStatsDTO;
import ma.femme.dto.FunAnswerCount;
import ma.femme.dto.FunKeyCount;
import ma.femme.dto.FunQuizState;
import ma.femme.entity.FunQuiz;
import ma.femme.entity.FunQuizAnswer;
import ma.femme.entity.FunQuizStatus;
import ma.femme.entity.Guest;
import ma.femme.entity.Invitation;
import ma.femme.entity.InvitationType;
import ma.femme.exception.GuestNotFoundException;
import ma.femme.repository.FunQuizAnswerRepository;
import ma.femme.repository.FunQuizRepository;
import ma.femme.repository.GuestRepository;

class AdminFunInvitationServiceTest {

  private GuestRepository guestRepository;
  private FunQuizRepository quizRepository;
  private FunQuizAnswerRepository answerRepository;
  private AdminFunInvitationService service;

  private Invitation invitation;
  private Guest juan;

  @BeforeEach
  void setUp() {
    guestRepository = mock(GuestRepository.class);
    quizRepository = mock(FunQuizRepository.class);
    answerRepository = mock(FunQuizAnswerRepository.class);
    service = new AdminFunInvitationService(guestRepository, quizRepository, answerRepository);

    invitation = Invitation.builder()
        .id(10L).type(InvitationType.FAMILY).referenceName("Familia Pérez").build();
    juan = Guest.builder().id(1L).fullName("Juan Pérez").build();
    invitation.addGuest(juan);

    when(guestRepository.findById(1L)).thenReturn(Optional.of(juan));
  }

  @Test
  void statsStatesAreExclusiveAndAddUpToTheTotal() {
    when(guestRepository.count()).thenReturn(10L);
    when(quizRepository.countByStatus(FunQuizStatus.IN_PROGRESS)).thenReturn(3L);
    when(quizRepository.countByStatus(FunQuizStatus.COMPLETED)).thenReturn(2L);
    when(answerRepository.countByQuestionAndOption()).thenReturn(List.of());
    when(quizRepository.countCompletedByResult()).thenReturn(List.of());

    AdminFunStatsDTO stats = service.getStats();

    assertThat(stats.totalGuests()).isEqualTo(10L);
    assertThat(stats.notStarted()).isEqualTo(5L);
    assertThat(stats.inProgress()).isEqualTo(3L);
    assertThat(stats.completed()).isEqualTo(2L);
    assertThat(stats.notStarted() + stats.inProgress() + stats.completed())
        .isEqualTo(stats.totalGuests());
    assertThat(stats.completionPercent()).isEqualTo(20.0);
  }

  @Test
  void statsWithoutGuestsDoNotDivideByZero() {
    when(guestRepository.count()).thenReturn(0L);
    when(quizRepository.countByStatus(FunQuizStatus.IN_PROGRESS)).thenReturn(0L);
    when(quizRepository.countByStatus(FunQuizStatus.COMPLETED)).thenReturn(0L);
    when(answerRepository.countByQuestionAndOption()).thenReturn(List.of());
    when(quizRepository.countCompletedByResult()).thenReturn(List.of());

    assertThat(service.getStats().completionPercent()).isZero();
  }

  @Test
  void statsCountEveryOptionAndVerdictFromTheDatabaseRows() {
    when(guestRepository.count()).thenReturn(4L);
    when(quizRepository.countByStatus(FunQuizStatus.IN_PROGRESS)).thenReturn(1L);
    when(quizRepository.countByStatus(FunQuizStatus.COMPLETED)).thenReturn(3L);
    when(answerRepository.countByQuestionAndOption()).thenReturn(List.of(
        new FunAnswerCount("attend", "outfit", 2L),
        new FunAnswerCount("attend", "love", 1L)));
    when(quizRepository.countCompletedByResult()).thenReturn(List.of(
        new FunKeyCount(FunQuizCatalog.V_WHITE_DRESS, 2L),
        new FunKeyCount(FunQuizCatalog.V_GENERAL, 1L)));

    AdminFunStatsDTO stats = service.getStats();

    AdminFunStatsDTO.QuestionStat attend = stats.questions().get(0);
    assertThat(attend.key()).isEqualTo("attend");
    assertThat(attend.totalAnswers()).isEqualTo(3L);
    assertThat(attend.options()).filteredOn(option -> option.key().equals("outfit"))
        .extracting(AdminFunStatsDTO.OptionStat::count).containsExactly(2L);
    // Las opciones que nadie eligió aparecen con 0.
    assertThat(attend.options()).filteredOn(option -> option.key().equals("obvio"))
        .extracting(AdminFunStatsDTO.OptionStat::count).containsExactly(0L);

    assertThat(stats.verdicts().get(0).key()).isEqualTo(FunQuizCatalog.V_WHITE_DRESS);
    assertThat(stats.verdicts().get(0).count()).isEqualTo(2L);
  }

  @Test
  void detailOfAGuestWhoNeverStartedHasNoAnswers() {
    when(quizRepository.findByGuestId(1L)).thenReturn(Optional.empty());

    AdminFunGuestDetailDTO detail = service.getDetail(1L);

    assertThat(detail.guest().state()).isEqualTo(FunQuizState.NOT_STARTED);
    assertThat(detail.guest().formalToken()).isEqualTo(invitation.getUniqueToken());
    assertThat(detail.guest().funToken()).isEqualTo(juan.getFunToken());
    assertThat(detail.answers()).isEmpty();
    assertThat(detail.verdict()).isNull();
  }

  @Test
  void detailShowsTheAnswersWithTheirTextsAndTheVerdict() {
    LocalDateTime now = LocalDateTime.now();
    FunQuiz quiz = FunQuiz.builder().id(5L).guest(juan).startedAt(now).updatedAt(now).build();
    quiz.markCompleted(FunQuizCatalog.V_WHITE_DRESS, now);
    when(quizRepository.findByGuestId(1L)).thenReturn(Optional.of(quiz));
    when(answerRepository.findByQuizId(5L)).thenReturn(List.of(
        FunQuizAnswer.builder().quiz(quiz).questionKey("attend").optionKey("outfit")
            .answeredAt(now).build()));

    AdminFunGuestDetailDTO detail = service.getDetail(1L);

    assertThat(detail.guest().state()).isEqualTo(FunQuizState.COMPLETED);
    assertThat(detail.answers()).hasSize(1);
    assertThat(detail.answers().get(0).option()).isEqualTo("¡Sí! Ya estoy buscando qué ponerme.");
    assertThat(detail.verdict().key()).isEqualTo(FunQuizCatalog.V_WHITE_DRESS);
  }

  @Test
  void regeneratingChangesOnlyTheFunTokenOfThatGuest() {
    UUID oldFunToken = juan.getFunToken();
    UUID formalToken = invitation.getUniqueToken();
    when(quizRepository.findByGuestIdIn(List.of(1L))).thenReturn(List.of());

    AdminFunGuestDTO result = service.regenerateToken(1L);

    assertThat(juan.getFunToken()).isNotEqualTo(oldFunToken);
    assertThat(result.funToken()).isEqualTo(juan.getFunToken());
    assertThat(invitation.getUniqueToken()).isEqualTo(formalToken);
  }

  @Test
  void unknownGuestsAreNotFound() {
    when(guestRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.getDetail(99L)).isInstanceOf(GuestNotFoundException.class);
    assertThatThrownBy(() -> service.regenerateToken(99L)).isInstanceOf(GuestNotFoundException.class);
  }
}
