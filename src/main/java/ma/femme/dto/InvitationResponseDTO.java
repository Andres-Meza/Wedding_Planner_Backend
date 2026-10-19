package ma.femme.dto;

import java.util.List;

import lombok.Builder;
import ma.femme.entity.InvitationStatus;
import ma.femme.entity.InvitationType;

@Builder
public record InvitationResponseDTO(
    Long id,
    InvitationType type,
    String referenceName,
    InvitationStatus status,
    List<GuestResponseDTO> guests,
    // Mensaje que esta invitación dejó para los novios (null si no hay).
    String coupleMessage) {
}
