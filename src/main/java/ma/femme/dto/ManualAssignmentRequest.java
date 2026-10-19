package ma.femme.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ManualAssignmentRequest(
    @NotNull Long guestId,
    @NotNull Long tableId,
    // Silla concreta (0 = primera). Opcional: sin ella el invitado queda en
    // la mesa y el plano le busca una silla libre.
    @Min(0) Integer seatIndex) {
}
