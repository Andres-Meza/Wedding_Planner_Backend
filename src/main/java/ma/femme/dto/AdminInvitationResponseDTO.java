package ma.femme.dto;

import lombok.Builder;
import ma.femme.entity.InvitationStatus;
import ma.femme.entity.InvitationType;

import java.util.List;
import java.util.UUID;

// Unlike InvitationResponseDTO (the one the guest sees through their
// public link), this includes the uniqueToken because only the admin
// needs to generate/copy the invitation link to send it.
@Builder
public record AdminInvitationResponseDTO(
        Long id,
        InvitationType type,
        UUID uniqueToken,
        String referenceName,
        String contactPhone,
        String contactEmail,
        InvitationStatus generalStatus,
        List<GuestResponseDTO> guests
) {
}