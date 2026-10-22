package ma.femme.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ma.femme.dto.AdminInvitationResponseDTO;
import ma.femme.dto.CreateGuestRequest;
import ma.femme.dto.CreateInvitationRequest;
import ma.femme.entity.Guest;
import ma.femme.entity.Invitation;
import ma.femme.entity.InvitationType;
import ma.femme.entity.Menu;
import ma.femme.exception.DefaultMenuUnavailableException;
import ma.femme.repository.InvitationRepository;

class AdminInvitationServiceTest {

  private InvitationRepository invitationRepository;
  private DefaultMenuService defaultMenuService;
  private AdminInvitationService service;

  private final Menu adultMenu = DefaultMenuServiceTest.menu(1L, "Menú 1", false, true);
  private final Menu kidsMenu = DefaultMenuServiceTest.menu(2L, "Menú Infantil 1", true, true);

  @BeforeEach
  void setUp() {
    invitationRepository = mock(InvitationRepository.class);
    defaultMenuService = mock(DefaultMenuService.class);
    service = new AdminInvitationService(invitationRepository, defaultMenuService);

    when(invitationRepository.save(any(Invitation.class))).thenAnswer(call -> call.getArgument(0));
    when(defaultMenuService.defaultMenuFor(false)).thenReturn(adultMenu);
    when(defaultMenuService.defaultMenuFor(true)).thenReturn(kidsMenu);
  }

  @Test
  void individualAdultGetsMenu1() {
    AdminInvitationResponseDTO result = service.createInvitation(new CreateInvitationRequest(
        InvitationType.INDIVIDUAL, "Ana", null, null,
        List.of(new CreateGuestRequest("Ana Gómez", false))));

    assertThat(result.guests()).singleElement()
        .satisfies(guest -> assertThat(guest.menu().name()).isEqualTo("Menú 1"));
  }

  @Test
  void individualMinorGetsKidsMenu1() {
    AdminInvitationResponseDTO result = service.createInvitation(new CreateInvitationRequest(
        InvitationType.INDIVIDUAL, "Leo", null, null,
        List.of(new CreateGuestRequest("Leo Pérez", true))));

    assertThat(result.guests()).singleElement()
        .satisfies(guest -> assertThat(guest.menu().name()).isEqualTo("Menú Infantil 1"));
  }

  @Test
  void familyAssignsEachMemberTheMenuOfTheirOwnCategory() {
    AdminInvitationResponseDTO result = service.createInvitation(new CreateInvitationRequest(
        InvitationType.FAMILY, "Familia Pérez", null, null,
        List.of(
            new CreateGuestRequest("Juan Pérez", false),
            new CreateGuestRequest("Marta Ruiz", false),
            new CreateGuestRequest("Leo Pérez", true))));

    assertThat(result.guests()).extracting(g -> g.fullName() + "=" + g.menu().name())
        .containsExactly(
            "Juan Pérez=Menú 1",
            "Marta Ruiz=Menú 1",
            "Leo Pérez=Menú Infantil 1");
  }

  @Test
  void listAllReadsInvitationsWithTheirGuestsInOneQuery() {
    Invitation invitation = Invitation.builder()
        .id(1L).type(InvitationType.INDIVIDUAL).referenceName("Ana").build();
    invitation.addGuest(Guest.builder().id(5L).fullName("Ana Gómez").menu(adultMenu).build());
    when(invitationRepository.findAllByOrderByIdAsc()).thenReturn(List.of(invitation));

    List<AdminInvitationResponseDTO> result = service.listAll();

    assertThat(result).singleElement().satisfies(dto -> {
      assertThat(dto.uniqueToken()).isEqualTo(invitation.getUniqueToken());
      assertThat(dto.guests()).extracting(guest -> guest.fullName()).containsExactly("Ana Gómez");
    });
    verify(invitationRepository, never()).findAll();
  }

  @Test
  void nothingIsSavedWhenTheDefaultMenuIsUnavailable() {
    when(defaultMenuService.defaultMenuFor(true))
        .thenThrow(new DefaultMenuUnavailableException("sin menú infantil"));

    assertThatThrownBy(() -> service.createInvitation(new CreateInvitationRequest(
        InvitationType.FAMILY, "Familia Pérez", null, null,
        List.of(
            new CreateGuestRequest("Juan Pérez", false),
            new CreateGuestRequest("Leo Pérez", true)))))
        .isInstanceOf(DefaultMenuUnavailableException.class);

    verify(invitationRepository, never()).save(any());
  }
}
