package ma.femme.dto;

import jakarta.validation.constraints.NotNull;

public record ManualAssignmentRequest(
        @NotNull Long guestId,
        @NotNull Long tableId
) {
}
