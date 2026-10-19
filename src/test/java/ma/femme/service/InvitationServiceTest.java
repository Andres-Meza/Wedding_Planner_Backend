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

import ma.femme.dto.AttendanceResponseRequest;
import ma.femme.dto.CoupleMessageRequest;
import ma.femme.dto.CoupleMessageResponseDTO;
import ma.femme.dto.GuestAttendanceResponseDTO;
import ma.femme.dto.InvitationResponseDTO;
import ma.femme.entity.AttendanceStatus;
import ma.femme.entity.Guest;
import ma.femme.entity.Invitation;
import ma.femme.entity.InvitationStatus;
import ma.femme.entity.InvitationType;
import ma.femme.entity.Menu;
import ma.femme.exception.DefaultMenuUnavailableException;
import ma.femme.exception.InvitationNotFoundException;
import ma.femme.repository.InvitationRepository;

class InvitationServiceTest {

  private final UUID token = UUID.randomUUID();
  private final Menu adultMenu = DefaultMenuServiceTest.menu(1L, "Menú 1", false, true);
  private final Menu kidsMenu = DefaultMenuServiceTest.menu(2L, "Menú Infantil 1", true, true);

  private InvitationRepository invitationRepository;
  private DefaultMenuService defaultMenuService;
  private InvitationService service;
  private Invitation invitation;
  private Guest adult;
  private Guest child;

  @BeforeEach
  void setUp() {
    invitationRepository = mock(InvitationRepository.class);
    defaultMenuService = mock(DefaultMenuService.class);
    service = new InvitationService(invitationRepository, defaultMenuService);

    invitation = Invitation.builder()
        .id(10L).type(InvitationType.FAMILY).referenceName("Familia Pérez").build();
    adult = Guest.builder().id(1L).fullName("Juan Pérez").isMinor(false).build();
    child = Guest.builder().id(2L).fullName("Leo Pérez").isMinor(true).build();
    invitation.addGuest(adult);
    invitation.addGuest(child);

    when(invitationRepository.findByUniqueToken(token)).thenReturn(Optional.of(invitation));
    when(defaultMenuService.defaultMenuFor(false)).thenReturn(adultMenu);
    when(defaultMenuService.defaultMenuFor(true)).thenReturn(kidsMenu);
  }

  // ---------- Menús ----------

  @Test
  void confirmingWithoutAnyMenuIdWorksAndKeepsTheAssignedMenu() {
    adult.setMenu(adultMenu);
    child.setMenu(kidsMenu);

    InvitationResponseDTO result = service.respondToAttendance(token, request(
        response(1L, AttendanceStatus.CONFIRMED),
        response(2L, AttendanceStatus.CONFIRMED)));

    assertThat(adult.getMenu()).isSameAs(adultMenu);
    assertThat(child.getMenu()).isSameAs(kidsMenu);
    assertThat(result.status()).isEqualTo(InvitationStatus.FULLY_RESPONDED);
  }

  @Test
  void legacyGuestWithoutMenuReceivesTheDefaultWhenConfirming() {
    service.respondToAttendance(token, request(
        response(1L, AttendanceStatus.CONFIRMED),
        response(2L, AttendanceStatus.CONFIRMED)));

    assertThat(adult.getMenu()).isSameAs(adultMenu);
    assertThat(child.getMenu()).isSameAs(kidsMenu);
  }

  @Test
  void existingMenuIsNeverOverwrittenEvenIfInactive() {
    Menu inactive = DefaultMenuServiceTest.menu(9L, "Pollo", false, false);
    adult.setMenu(inactive);

    service.respondToAttendance(token, request(response(1L, AttendanceStatus.CONFIRMED)));

    assertThat(adult.getMenu()).isSameAs(inactive);
  }

  @Test
  void notAttendingKeepsTheAssignedMenuAndDoesNotAssignOne() {
    adult.setMenu(adultMenu);

    service.respondToAttendance(token, request(
        response(1L, AttendanceStatus.NOT_ATTENDING),
        response(2L, AttendanceStatus.NOT_ATTENDING)));

    assertThat(adult.getAttendance()).isEqualTo(AttendanceStatus.NOT_ATTENDING);
    assertThat(adult.getMenu()).isSameAs(adultMenu);
    assertThat(child.getMenu()).isNull();
  }

  @Test
  void partialResponseLeavesTheRestPendingAndTheMenuAssignmentIsNotAConfirmation() {
    adult.setMenu(adultMenu);
    child.setMenu(kidsMenu);

    InvitationResponseDTO result = service.respondToAttendance(token, request(
        response(1L, AttendanceStatus.CONFIRMED)));

    assertThat(result.status()).isEqualTo(InvitationStatus.PARTIALLY_RESPONDED);
    assertThat(child.getAttendance()).isEqualTo(AttendanceStatus.NO_RESPONSE);
  }

  @Test
  void unavailableDefaultMenuIsReportedWhenAGuestWithoutMenuConfirms() {
    when(defaultMenuService.defaultMenuFor(false))
        .thenThrow(new DefaultMenuUnavailableException("sin menú"));

    assertThatThrownBy(() -> service.respondToAttendance(token, request(
        response(1L, AttendanceStatus.CONFIRMED))))
        .isInstanceOf(DefaultMenuUnavailableException.class);
  }

  // ---------- Mensaje para los novios ----------

  @Test
  void invitationStartsWithoutMessage() {
    assertThat(service.getByToken(token).coupleMessage()).isNull();
  }

  @Test
  void savesAndReturnsTheMessage() {
    CoupleMessageResponseDTO saved = service.saveCoupleMessage(
        token, new CoupleMessageRequest("  ¡Felicidades!  "));

    assertThat(saved.message()).isEqualTo("¡Felicidades!");
    assertThat(saved.updatedAt()).isNotNull();
    assertThat(service.getByToken(token).coupleMessage()).isEqualTo("¡Felicidades!");
  }

  @Test
  void savingAgainReplacesTheSameMessageAndBumpsTheDate() {
    service.saveCoupleMessage(token, new CoupleMessageRequest("primero"));
    invitation.setCoupleMessageUpdatedAt(LocalDateTime.now().minusDays(1));
    LocalDateTime before = invitation.getCoupleMessageUpdatedAt();

    service.saveCoupleMessage(token, new CoupleMessageRequest("segundo"));

    assertThat(invitation.getCoupleMessage()).isEqualTo("segundo");
    assertThat(invitation.getCoupleMessageUpdatedAt()).isAfter(before);
  }

  @Test
  void resendingTheSameTextDoesNotChangeTheDate() {
    service.saveCoupleMessage(token, new CoupleMessageRequest("hola"));
    LocalDateTime first = LocalDateTime.now().minusDays(2);
    invitation.setCoupleMessageUpdatedAt(first);

    service.saveCoupleMessage(token, new CoupleMessageRequest("hola"));

    assertThat(invitation.getCoupleMessageUpdatedAt()).isEqualTo(first);
  }

  @Test
  void blankMessageClearsItAndTheDate() {
    service.saveCoupleMessage(token, new CoupleMessageRequest("hola"));

    CoupleMessageResponseDTO cleared = service.saveCoupleMessage(
        token, new CoupleMessageRequest("   "));

    assertThat(cleared.message()).isNull();
    assertThat(cleared.updatedAt()).isNull();
  }

  @Test
  void rejectsMessagesLongerThanTheLimit() {
    String tooLong = "a".repeat(Invitation.COUPLE_MESSAGE_MAX_LENGTH + 1);

    assertThatThrownBy(() -> service.saveCoupleMessage(token, new CoupleMessageRequest(tooLong)))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void acceptsMessageOfExactlyTheLimit() {
    String max = "a".repeat(Invitation.COUPLE_MESSAGE_MAX_LENGTH);

    assertThat(service.saveCoupleMessage(token, new CoupleMessageRequest(max)).message())
        .hasSize(Invitation.COUPLE_MESSAGE_MAX_LENGTH);
  }

  @Test
  void respondingToAttendanceDoesNotTouchTheMessage() {
    service.saveCoupleMessage(token, new CoupleMessageRequest("con cariño"));
    adult.setMenu(adultMenu);

    service.respondToAttendance(token, request(response(1L, AttendanceStatus.CONFIRMED)));

    assertThat(invitation.getCoupleMessage()).isEqualTo("con cariño");
  }

  @Test
  void unknownTokenIsReportedAsNotFound() {
    UUID other = UUID.randomUUID();
    when(invitationRepository.findByUniqueToken(other)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.saveCoupleMessage(other, new CoupleMessageRequest("hola")))
        .isInstanceOf(InvitationNotFoundException.class);
  }

  // ---------- Utilidades ----------

  private static GuestAttendanceResponseDTO response(Long guestId, AttendanceStatus attendance) {
    return new GuestAttendanceResponseDTO(guestId, attendance, null, null);
  }

  private static AttendanceResponseRequest request(GuestAttendanceResponseDTO... responses) {
    return new AttendanceResponseRequest("Juan Pérez", List.of(responses));
  }
}
