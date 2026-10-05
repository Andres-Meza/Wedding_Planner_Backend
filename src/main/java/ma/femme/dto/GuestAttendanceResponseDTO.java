package ma.femme.dto;

import jakarta.validation.constraints.NotNull;
import ma.femme.entity.AttendanceStatus;

public record GuestAttendanceResponseDTO(
        @NotNull Long guestId,
        @NotNull AttendanceStatus attendance,
        String dietaryRestriction,
        // Obligatorio solo cuando attendance = CONFIRMED; se valida en el
        // servicio contra MenuOption o KidsMenuOption según isMinor.
        String selectedMenu
) {
}
