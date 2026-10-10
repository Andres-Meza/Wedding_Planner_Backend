package ma.femme.service;

import lombok.RequiredArgsConstructor;
import ma.femme.dto.AttendanceResponseRequest;
import ma.femme.dto.GuestAttendanceResponseDTO;
import ma.femme.dto.GuestResponseDTO;
import ma.femme.dto.InvitationResponseDTO;
import ma.femme.dto.MenuResponseDTO;
import ma.femme.entity.AllergyType;
import ma.femme.entity.AttendanceStatus;
import ma.femme.entity.Guest;
import ma.femme.entity.Invitation;
import ma.femme.entity.InvitationStatus;
import ma.femme.entity.Menu;
import ma.femme.exception.InvitationNotFoundException;
import ma.femme.repository.InvitationRepository;
import ma.femme.repository.MenuRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvitationService {

  private final InvitationRepository invitationRepository;
  private final MenuRepository menuRepository;

  @Transactional(readOnly = true)
  public InvitationResponseDTO getByToken(UUID token) {
    Invitation invitation = findByTokenOrThrow(token);
    return mapToDto(invitation);
  }

  @Transactional
  public InvitationResponseDTO respondToAttendance(
    UUID token,
    AttendanceResponseRequest request) {

    Invitation invitation = findByTokenOrThrow(token);

    Map<Long, Guest> guestsById = invitation.getGuests().stream()
      .collect(Collectors.toMap(Guest::getId, guest -> guest));

    for (GuestAttendanceResponseDTO response : request.responses()) {
      Guest guest = guestsById.get(response.guestId());

      if (guest == null) {
        throw new IllegalArgumentException(
          "Guest " + response.guestId() + " does not belong to this invitation");
      }

      guest.setAttendance(response.attendance());
      guest.setAllergyType(response.allergyType());
      guest.setDietaryRestriction(response.dietaryRestriction());
      guest.setRespondedBy(request.respondedBy());
      guest.setResponseDate(LocalDateTime.now());

      if (response.attendance() == AttendanceStatus.CONFIRMED) {
        validateAllergy(guest, response);
        guest.setMenu(resolveMenu(guest, response.menuId()));
      } else {
        // Si no asiste, cualquier menú que haya quedado guardado de
        // antes ya no aplica.
        guest.setMenu(null);
      }
    }

    updateOverallStatus(invitation);

    // JPA synchronizes the changes when the transaction completes
    // through dirty checking; an explicit save is not required here.
    return mapToDto(invitation);
  }

  // Una alergia "Otra" no dice nada por sí sola: hay que describirla.
  private void validateAllergy(Guest guest, GuestAttendanceResponseDTO response) {
    boolean noDescription = response.dietaryRestriction() == null
      || response.dietaryRestriction().isBlank();

    if (response.allergyType() == AllergyType.OTHER && noDescription) {
      throw new IllegalArgumentException(
        "Describe la alergia de " + guest.getFullName() + " en las observaciones");
    }
  }

  // Un invitado que confirma asistencia debe elegir un menú existente, de
  // su categoría (adulto o infantil) y disponible. Si ya lo tenía elegido y
  // luego lo desactivaron, puede conservarlo al modificar su RSVP.
  private Menu resolveMenu(Guest guest, Long menuId) {
    if (menuId == null) {
      throw new IllegalArgumentException(
        "Debes elegir un menú para " + guest.getFullName());
    }

    Menu menu = menuRepository.findById(menuId)
      .orElseThrow(() -> new IllegalArgumentException(
        "El menú elegido para " + guest.getFullName() + " no existe"));

    if (menu.isKids() != guest.isMinor()) {
      throw new IllegalArgumentException(
        "El menú '" + menu.getName() + "' no corresponde a la categoría de "
          + guest.getFullName());
    }

    boolean alreadySelected = guest.getMenu() != null
      && guest.getMenu().getId().equals(menu.getId());

    if (!menu.isActive() && !alreadySelected) {
      throw new IllegalArgumentException(
        "El menú '" + menu.getName() + "' ya no está disponible");
    }

    return menu;
  }

  private void updateOverallStatus(Invitation invitation) {
    long totalGuests = invitation.getGuests().size();

    long unansweredGuests = invitation.getGuests().stream()
      .filter(guest -> guest.getAttendance() == AttendanceStatus.NO_RESPONSE)
      .count();

    InvitationStatus newStatus;

    if (unansweredGuests == totalGuests) {
      newStatus = InvitationStatus.PENDING;
    } else if (unansweredGuests == 0) {
      newStatus = InvitationStatus.FULLY_RESPONDED;
    } else {
      newStatus = InvitationStatus.PARTIALLY_RESPONDED;
    }

    invitation.setStatus(newStatus);
  }

  private Invitation findByTokenOrThrow(UUID token) {
    return invitationRepository.findByUniqueToken(token)
      .orElseThrow(() -> new InvitationNotFoundException(token));
  }

  private InvitationResponseDTO mapToDto(Invitation invitation) {

    List<GuestResponseDTO> guests = invitation.getGuests().stream()
      .map(guest -> GuestResponseDTO.builder()
        .id(guest.getId())
        .fullName(guest.getFullName())
        .isMinor(guest.isMinor())
        .attendance(guest.getAttendance())
        .allergyType(guest.getAllergyType())
        .dietaryRestriction(guest.getDietaryRestriction())
        .menu(MenuResponseDTO.fromEntity(guest.getMenu()))
        .build())
      .toList();

    return InvitationResponseDTO.builder()
      .id(invitation.getId())
      .type(invitation.getType())
      .referenceName(invitation.getReferenceName())
      .status(invitation.getStatus())
      .guests(guests)
      .build();
  }
}
