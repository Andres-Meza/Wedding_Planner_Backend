package ma.femme.dto;

import lombok.Builder;
import ma.femme.entity.InvitationStatus;
import ma.femme.entity.InvitationType;

import java.util.List;

@Builder
public record InvitationResponseDTO(
        Long id,
        InvitationType type,
        String referenceName,
        InvitationStatus status,
        List<GuestResponseDTO> guests,
        // Mensaje que esta invitación dejó para los novios (null si no hay).
        String coupleMessage
) {
}
