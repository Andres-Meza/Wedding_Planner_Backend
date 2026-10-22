package ma.femme.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ma.femme.dto.AdminInvitationResponseDTO;
import ma.femme.dto.CreateGuestRequest;
import ma.femme.dto.CreateInvitationRequest;
import ma.femme.dto.GuestResponseDTO;
import ma.femme.entity.Guest;
import ma.femme.entity.Invitation;
import ma.femme.entity.InvitationType;
import ma.femme.repository.InvitationRepository;

@Service
@RequiredArgsConstructor
public class AdminInvitationService {

  private final InvitationRepository invitationRepository;
  private final DefaultMenuService defaultMenuService;

  @Transactional
  public AdminInvitationResponseDTO createInvitation(
      CreateInvitationRequest request) {

    if (request.type() == InvitationType.INDIVIDUAL && request.guests().size() > 1) {
      throw new IllegalArgumentException(
          "An individual invitation can only have one guest; use FAMILY for more than one");
    }

    Invitation invitation = Invitation.builder()
        .type(request.type())
        .referenceName(request.referenceName())
        .contactPhone(request.contactPhone())
        .contactEmail(request.contactEmail())
        .build();

    // Si falta el menú por defecto de algún invitado, la transacción
    // completa se revierte: no se guarda nada a medias.
    for (CreateGuestRequest guestRequest : request.guests()) {
      invitation.addGuest(newGuest(guestRequest));
    }

    Invitation saved = invitationRepository.save(invitation);

    return mapToAdminDTO(saved);
  }

  @Transactional
  public AdminInvitationResponseDTO addGuest(
      Long invitationId,
      CreateGuestRequest request) {

    Invitation invitation = invitationRepository.findById(invitationId)
        .orElseThrow(() -> new IllegalArgumentException(
            "No invitation exists with id " + invitationId));

    if (invitation.getType() == InvitationType.INDIVIDUAL && !invitation.getGuests().isEmpty()) {
      throw new IllegalArgumentException(
          "This is an individual invitation and already has a guest; use FAMILY for more than one");
    }

    invitation.addGuest(newGuest(request));

    return mapToAdminDTO(invitation);
  }

  @Transactional(readOnly = true)
  public List<AdminInvitationResponseDTO> listAll() {
    return invitationRepository.findAllByOrderByIdAsc().stream()
        .map(this::mapToAdminDTO)
        .toList();
  }

  @Transactional(readOnly = true)
  public AdminInvitationResponseDTO getById(Long id) {
    Invitation invitation = invitationRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException(
            "No invitation exists with id " + id));

    return mapToAdminDTO(invitation);
  }

  // El menú lo asigna el backend según la categoría de cada invitado
  // (adulto -> Menú 1, menor -> Menú Infantil 1).
  private Guest newGuest(CreateGuestRequest request) {
    return Guest.builder()
        .fullName(request.fullName())
        .isMinor(request.isMinor())
        .menu(defaultMenuService.defaultMenuFor(request.isMinor()))
        .build();
  }

  private AdminInvitationResponseDTO mapToAdminDTO(Invitation invitation) {

    List<GuestResponseDTO> guests = invitation.getGuests().stream()
        .map(GuestResponseDTO::fromEntity)
        .toList();

    return AdminInvitationResponseDTO.builder()
        .id(invitation.getId())
        .type(invitation.getType())
        .uniqueToken(invitation.getUniqueToken())
        .referenceName(invitation.getReferenceName())
        .contactPhone(invitation.getContactPhone())
        .contactEmail(invitation.getContactEmail())
        .generalStatus(invitation.getStatus())
        .guests(guests)
        .build();
  }
}
