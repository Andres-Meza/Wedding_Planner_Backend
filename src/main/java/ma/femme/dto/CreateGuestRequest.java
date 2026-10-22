package ma.femme.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateGuestRequest(
    @NotBlank @Size(max = 150) String fullName,
    boolean isMinor) {
}
