package ma.femme.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateGuestRequest(
    @NotBlank String fullName,
    boolean isMinor) {
}
