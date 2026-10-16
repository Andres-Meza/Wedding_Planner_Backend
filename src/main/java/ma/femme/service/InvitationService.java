package ma.femme.service;

import lombok.RequiredArgsConstructor;
import ma.femme.dto.AttendanceResponseRequest;
import ma.femme.dto.CoupleMessageRequest;
import ma.femme.dto.CoupleMessageResponseDTO;
import ma.femme.dto.GuestAttendanceResponseDTO;
import ma.femme.dto.GuestResponseDTO;
import ma.femme.dto.InvitationResponseDTO;
import ma.femme.dto.MenuResponseDTO;
import ma.femme.entity.AllergyType;
import ma.femme.entity.AttendanceStatus;
import ma.femme.entity.Guest;
import ma.femme.entity.Invitation;
import ma.femme.entity.InvitationStatus;
import ma.femme.exception.InvitationNotFoundException;
import ma.femme.repository.InvitationRepository;
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
  private final DefaultMenuService defaultMenuService;

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
        ensureMenuAssigned(guest);
      }
      // El menú ya NO lo elige el invitado: lo asigna el sistema al crear
      // al invitado. Responder "no asisto" tampoco lo borra; el reporte de
      // alimentación solo cuenta a los confirmados.
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

  // Todo invitado nuevo ya nace con su menú por defecto. Esto cubre a los
  // invitados anteriores que no lo tengan (por ejemplo, si los menús por
  // defecto aún no existían cuando se aplicó la migración V7): se les asigna
  // al confirmar. Si ya tienen uno (aunque luego lo hayan desactivado o el
  // admin lo haya cambiado a mano), se respeta. Si el menú por defecto no
  // está disponible, lanza DefaultMenuUnavailableException y no se guarda
  // ninguna respuesta de esta solicitud.
  private void ensureMenuAssigned(Guest guest) {
    if (guest.getMenu() == null) {
      guest.setMenu(defaultMenuService.defaultMenuFor(guest.isMinor()));
    }
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

  // Guarda (o reemplaza) el mensaje de la invitación para los novios. Es
  // idempotente: siempre actualiza la misma columna de la misma invitación,
  // así que reintentos o envíos repetidos no crean duplicados. Un mensaje
  // vacío lo borra. No toca la asistencia ni los invitados.
  @Transactional
  public CoupleMessageResponseDTO saveCoupleMessage(
    UUID token,
    CoupleMessageRequest request) {

    Invitation invitation = findByTokenOrThrow(token);

    String message = MessageText.normalize(request.message());

    if (message != null && message.length() > Invitation.COUPLE_MESSAGE_MAX_LENGTH) {
      throw new IllegalArgumentException(
        "El mensaje no puede superar los " + Invitation.COUPLE_MESSAGE_MAX_LENGTH + " caracteres");
    }

    invitation.updateCoupleMessage(message, LocalDateTime.now());

    return new CoupleMessageResponseDTO(
      invitation.getCoupleMessage(),
      invitation.coupleMessageUpdatedAtInstant());
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
      .coupleMessage(invitation.getCoupleMessage())
      .build();
  }
}
