package ma.femme.service;

import lombok.RequiredArgsConstructor;
import ma.femme.dto.AttendanceResponseRequest;
import ma.femme.dto.GuestAttendanceResponseDTO;
import ma.femme.dto.GuestResponseDTO;
import ma.femme.dto.InvitationResponseDTO;
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
            guest.setDietaryRestriction(response.dietaryRestriction());
            guest.setRespondedBy(request.respondedBy());
            guest.setResponseDate(LocalDateTime.now());
        }

        updateOverallStatus(invitation);

        // JPA synchronizes the changes when the transaction completes
        // through dirty checking; an explicit save is not required here.
        return mapToDto(invitation);
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
                        .dietaryRestriction(guest.getDietaryRestriction())
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