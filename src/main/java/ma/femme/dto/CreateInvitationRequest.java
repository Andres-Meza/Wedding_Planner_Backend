package ma.femme.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import ma.femme.entity.InvitationType;

public record CreateInvitationRequest(
    @NotNull InvitationType type,
    @NotBlank String referenceName,
    String contactPhone,
    @Email String contactEmail,
    @NotEmpty @Valid List<CreateGuestRequest> guests) {
}
