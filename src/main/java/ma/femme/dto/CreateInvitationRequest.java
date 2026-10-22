package ma.femme.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ma.femme.entity.InvitationType;

public record CreateInvitationRequest(
    @NotNull InvitationType type,
    @NotBlank @Size(max = 150) String referenceName,
    @Size(max = 30) String contactPhone,
    @Email @Size(max = 150) String contactEmail,
    @NotEmpty @Valid List<CreateGuestRequest> guests) {
}
