package ma.femme.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import ma.femme.dto.GuestMessageFilter;
import ma.femme.dto.GuestMessagePageDTO;
import ma.femme.entity.Guest;
import ma.femme.entity.Invitation;
import ma.femme.entity.InvitationType;
import ma.femme.repository.InvitationRepository;

class GuestMessageServiceTest {

  private InvitationRepository repository;
  private GuestMessageService service;

  @BeforeEach
  void setUp() {
    repository = mock(InvitationRepository.class);
    service = new GuestMessageService(repository);
    when(repository.count()).thenReturn(10L);
    when(repository.countByCoupleMessageIsNotNull()).thenReturn(4L);
  }

  @Test
  void mapsRowsAndReturnsGeneralCounters() {
    Invitation family = Invitation.builder()
        .id(7L).type(InvitationType.FAMILY).referenceName("Familia Pérez").build();
    family.addGuest(Guest.builder().id(1L).fullName("Juan").build());
    family.addGuest(Guest.builder().id(2L).fullName("Leo").isMinor(true).build());
    LocalDateTime sent = LocalDateTime.of(2026, 10, 1, 18, 30);
    family.updateCoupleMessage("¡Felicidades!", sent);

    when(repository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(family), org.springframework.data.domain.PageRequest.of(0, 10), 1));

    GuestMessagePageDTO page = service.list(0, 10, null, null, null);

    assertThat(page.content()).singleElement().satisfies(row -> {
      assertThat(row.invitationId()).isEqualTo(7L);
      assertThat(row.referenceName()).isEqualTo("Familia Pérez");
      assertThat(row.type()).isEqualTo(InvitationType.FAMILY);
      assertThat(row.guestCount()).isEqualTo(2);
      assertThat(row.message()).isEqualTo("¡Felicidades!");
      assertThat(row.messageUpdatedAt())
          .isEqualTo(sent.atZone(java.time.ZoneId.systemDefault()).toInstant());
    });
    assertThat(page.totalInvitations()).isEqualTo(10);
    assertThat(page.withMessageCount()).isEqualTo(4);
    assertThat(page.withoutMessageCount()).isEqualTo(6);
  }

  @Test
  void defaultsToMessagesNewestFirstAndClampsThePageSize() {
    when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());

    service.list(-3, 5000, "  ", null, GuestMessageFilter.WITH_MESSAGE);

    ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
    verify(repository).findAll(any(Specification.class), captor.capture());
    Pageable pageable = captor.getValue();
    assertThat(pageable.getPageNumber()).isZero();
    assertThat(pageable.getPageSize()).isEqualTo(GuestMessageService.MAX_PAGE_SIZE);
    assertThat(pageable.getSort().getOrderFor("coupleMessageUpdatedAt").isDescending()).isTrue();
  }

  @Test
  void invitationsWithoutMessageAreSortedByCreationDate() {
    when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());

    service.list(0, 10, null, null, GuestMessageFilter.WITHOUT_MESSAGE);

    ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
    verify(repository).findAll(any(Specification.class), captor.capture());
    assertThat(captor.getValue().getSort().getOrderFor("creationDate").isDescending()).isTrue();
  }
}
