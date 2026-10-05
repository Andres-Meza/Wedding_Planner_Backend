package ma.femme.dto;

import lombok.Builder;

@Builder
public record AssignedGuestDTO(
        Long guestId,
        String fullName
) {
}
