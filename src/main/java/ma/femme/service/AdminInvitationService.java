package ma.femme.service;

import lombok.RequiredArgsConstructor;
import ma.femme.dto.AdminInvitationResponseDTO;
import ma.femme.dto.CreateGuestRequest;
import ma.femme.dto.CreateInvitationRequest;
import ma.femme.dto.GuestResponseDTO;
import ma.femme.entity.Guest;
import ma.femme.entity.Invitation;
import ma.femme.repository.InvitationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminInvitationService {

    private final InvitationRepository invitationRepository;

    @Transactional
    public AdminInvitationResponseDTO createInvitation(
            CreateInvitationRequest request) {

        Invitation invitation = Invitation.builder()
                .type(request.type())
                .referenceName(request.referenceName())
                .contactPhone(request.contactPhone())
                .contactEmail(request.contactEmail())
                .build();

        for (CreateGuestRequest guestRequest : request.guests()) {
            Guest guest = Guest.builder()
                    .fullName(guestRequest.fullName())
                    .isMinor(guestRequest.isMinor())
                    .build();

            invitation.addGuest(guest);
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

        Guest guest = Guest.builder()
                .fullName(request.fullName())
                .isMinor(request.isMinor())
                .build();

        invitation.addGuest(guest);

        return mapToAdminDTO(invitation);
    }

    @Transactional(readOnly = true)
    public List<AdminInvitationResponseDTO> listAll() {
        return invitationRepository.findAll().stream()
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

    private AdminInvitationResponseDTO mapToAdminDTO(Invitation invitation) {

        List<GuestResponseDTO> guests = invitation.getGuests().stream()
                .map(guest -> GuestResponseDTO.builder()
                        .id(guest.getId())
                        .fullName(guest.getFullName())
                        .isMinor(guest.isMinor())
                        .attendance(guest.getAttendance())
                        .dietaryRestriction(guest.getDietaryRestriction())
                        .build())
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