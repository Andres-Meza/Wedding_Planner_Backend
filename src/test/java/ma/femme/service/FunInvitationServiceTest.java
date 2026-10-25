package ma.femme.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import ma.femme.dto.FunInvitationDTO;
import ma.femme.dto.FunQuizState;
import ma.femme.entity.FunQuiz;
import ma.femme.entity.FunQuizAnswer;
import ma.femme.entity.Guest;
import ma.femme.exception.FunInvitationNotFoundException;
import ma.femme.exception.FunQuizCompletedException;
import ma.femme.repository.FunQuizAnswerRepository;
import ma.femme.repository.FunQuizRepository;
import ma.femme.repository.GuestRepository;

class FunInvitationServiceTest {

  private GuestRepository guestRepository;
  private FunQuizRepository quizRepository;
  private FunQuizAnswerRepository answerRepository;
  private FunInvitationService service;

  private Guest juan;
  private Guest pepito;
  private FunQuiz juanQuiz;

  // Las respuestas "guardadas" de Juan; los mocks de repositorio las leen.
  private final List<FunQuizAnswer> juanAnswers = new ArrayList<>();

  @BeforeEach
  void setUp() {
    guestRepository = mock(GuestRepository.class);
    quizRepository = mock(FunQuizRepository.class);
    answerRepository = mock(FunQuizAnswerRepository.class);
    service = new FunInvitationService(guestRepository, quizRepository, answerRepository);

    // Dos invitados de la MISMA familia, cada uno con su token propio.
    juan = Guest.builder().id(1L).fullName("Juan Pérez").build();
    pepito = Guest.builder().id(2L).fullName("Pepito Pérez").build();

    LocalDateTime now = LocalDateTime.now();
    juanQuiz = FunQuiz.builder().id(5L).guest(juan).startedAt(now).updatedAt(now).build();

    when(guestRepository.findByFunToken(juan.getFunToken())).thenReturn(Optional.of(juan));
    when(guestRepository.findByFunToken(pepito.getFunToken())).thenReturn(Optional.of(pepito));
    when(quizRepository.findByGuestId(1L)).thenReturn(Optional.of(juanQuiz));
    when(quizRepository.findByGuestIdForUpdate(1L)).thenReturn(Optional.of(juanQuiz));
    when(quizRepository.findByGuestId(2L)).thenReturn(Optional.empty());
    when(answerRepository.findByQuizId(5L)).thenAnswer(invocation -> List.copyOf(juanAnswers));
  }

  private FunQuizAnswer answer(String questionKey, String optionKey) {
    FunQuizAnswer answer = FunQuizAnswer.builder()
        .quiz(juanQuiz).questionKey(questionKey).optionKey(optionKey)
        .answeredAt(LocalDateTime.now()).build();
    juanAnswers.add(answer);
    return answer;
  }

  private void answerEverything() {
    answer("attend", "love");
    answer("excitement", "countdown");
    answer("tears", "maybe_not");
    answer("gift", "cash");
    answer("dress", "blue");
  }

  @Test
  void unknownTokenIsNotFoundWithoutEchoingIt() {
    UUID unknown = UUID.randomUUID();
    when(guestRepository.findByFunToken(unknown)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.getByToken(unknown))
        .isInstanceOf(FunInvitationNotFoundException.class)
        .hasMessageNotContaining(unknown.toString());
  }

  @Test
  void aGuestWhoNeverAnsweredIsNotStartedAndSeesTheCatalog() {
    FunInvitationDTO dto = service.getByToken(pepito.getFunToken());

    assertThat(dto.guestName()).isEqualTo("Pepito Pérez");
    assertThat(dto.state()).isEqualTo(FunQuizState.NOT_STARTED);
    assertThat(dto.answers()).isEmpty();
    assertThat(dto.questions()).hasSize(FunQuizCatalog.QUESTIONS.size());
    assertThat(dto.verdict()).isNull();
  }

  @Test
  void eachTokenOnlyShowsItsOwnGuestsAnswers() {
    answer("attend", "love");

    FunInvitationDTO juanView = service.getByToken(juan.getFunToken());
    FunInvitationDTO pepitoView = service.getByToken(pepito.getFunToken());

    assertThat(juanView.guestName()).isEqualTo("Juan Pérez");
    assertThat(juanView.answers()).containsEntry("attend", "love");
    assertThat(pepitoView.guestName()).isEqualTo("Pepito Pérez");
    assertThat(pepitoView.answers()).isEmpty();
  }

  @Test
  void savingTheFirstAnswerCreatesTheQuizAndTheAnswer() {
    FunInvitationDTO dto = service.saveAnswer(juan.getFunToken(), "attend", "outfit");

    verify(quizRepository).insertIfAbsent(eq(1L), any(LocalDateTime.class));
    ArgumentCaptor<FunQuizAnswer> saved = ArgumentCaptor.forClass(FunQuizAnswer.class);
    verify(answerRepository).save(saved.capture());
    assertThat(saved.getValue().getQuestionKey()).isEqualTo("attend");
    assertThat(saved.getValue().getOptionKey()).isEqualTo("outfit");
    assertThat(saved.getValue().getQuiz()).isSameAs(juanQuiz);
    assertThat(dto.state()).isEqualTo(FunQuizState.IN_PROGRESS);
  }

  @Test
  void changingAnAnswerUpdatesTheSameRowInsteadOfAddingAnother() {
    FunQuizAnswer existing = answer("attend", "love");
    when(answerRepository.findByQuizIdAndQuestionKey(5L, "attend")).thenReturn(Optional.of(existing));

    service.saveAnswer(juan.getFunToken(), "attend", "outfit");

    assertThat(existing.getOptionKey()).isEqualTo("outfit");
    verify(answerRepository, never()).save(any());
  }

  @Test
  void repeatingTheSameAnswerChangesNothing() {
    FunQuizAnswer existing = answer("attend", "love");
    LocalDateTime answeredAt = existing.getAnsweredAt();
    LocalDateTime quizUpdatedAt = juanQuiz.getUpdatedAt();
    when(answerRepository.findByQuizIdAndQuestionKey(5L, "attend")).thenReturn(Optional.of(existing));

    service.saveAnswer(juan.getFunToken(), "attend", "love");

    assertThat(existing.getAnsweredAt()).isEqualTo(answeredAt);
    assertThat(juanQuiz.getUpdatedAt()).isEqualTo(quizUpdatedAt);
    verify(answerRepository, never()).save(any());
  }

  @Test
  void invalidQuestionsAndOptionsAreRejectedBeforeTouchingTheDatabase() {
    assertThatThrownBy(() -> service.saveAnswer(juan.getFunToken(), "nope", "love"))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> service.saveAnswer(juan.getFunToken(), "attend", "nope"))
        .isInstanceOf(IllegalArgumentException.class);
    // Una opción válida de OTRA pregunta tampoco vale.
    assertThatThrownBy(() -> service.saveAnswer(juan.getFunToken(), "attend", "cash"))
        .isInstanceOf(IllegalArgumentException.class);

    verify(quizRepository, never()).insertIfAbsent(any(), any());
    verify(answerRepository, never()).save(any());
  }

  @Test
  void answersCannotBeChangedOnceTheQuizIsCompleted() {
    juanQuiz.markCompleted(FunQuizCatalog.V_GENERAL, LocalDateTime.now());

    assertThatThrownBy(() -> service.saveAnswer(juan.getFunToken(), "attend", "outfit"))
        .isInstanceOf(FunQuizCompletedException.class);
    verify(answerRepository, never()).save(any());
  }

  @Test
  void completingWithoutAQuizOrWithMissingAnswersIsRejected() {
    when(quizRepository.findByGuestIdForUpdate(2L)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.complete(pepito.getFunToken()))
        .isInstanceOf(IllegalArgumentException.class);

    answer("attend", "love");
    assertThatThrownBy(() -> service.complete(juan.getFunToken()))
        .isInstanceOf(IllegalArgumentException.class);
    assertThat(juanQuiz.isCompleted()).isFalse();
  }

  @Test
  void completingStoresTheVerdictFromTheResolverRules() {
    answer("attend", "love");
    answer("excitement", "countdown");
    answer("tears", "maybe_not");
    answer("gift", "home");
    answer("dress", "white");

    FunInvitationDTO dto = service.complete(juan.getFunToken());

    assertThat(juanQuiz.isCompleted()).isTrue();
    assertThat(juanQuiz.getResultKey()).isEqualTo(FunQuizCatalog.V_WHITE_DRESS);
    assertThat(juanQuiz.getCompletedAt()).isNotNull();
    assertThat(dto.state()).isEqualTo(FunQuizState.COMPLETED);
    assertThat(dto.verdict().key()).isEqualTo(FunQuizCatalog.V_WHITE_DRESS);
    assertThat(dto.verdict().approved()).isFalse();
  }

  @Test
  void completingTwiceKeepsTheSameVerdictAndDate() {
    answerEverything();

    FunInvitationDTO first = service.complete(juan.getFunToken());
    LocalDateTime completedAt = juanQuiz.getCompletedAt();
    FunInvitationDTO second = service.complete(juan.getFunToken());

    assertThat(second.verdict()).isEqualTo(first.verdict());
    assertThat(juanQuiz.getCompletedAt()).isEqualTo(completedAt);
  }

  @Test
  void aVerdictRemovedFromTheCatalogFallsBackToTheGeneralOne() {
    answerEverything();
    juanQuiz.markCompleted("RETIRED_VERDICT", LocalDateTime.now());

    FunInvitationDTO dto = service.getByToken(juan.getFunToken());

    assertThat(dto.verdict().key()).isEqualTo(FunQuizCatalog.V_GENERAL);
  }
}
