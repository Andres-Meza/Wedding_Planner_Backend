package ma.femme.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ma.femme.entity.AllergyType;
import ma.femme.entity.AttendanceStatus;

public record GuestAttendanceResponseDTO(
  @NotNull Long guestId,
  @NotNull AttendanceStatus attendance,
  // null = sin alergias.
  AllergyType allergyType,
  // Observaciones / restricciones alimentarias (texto libre).
  @Size(max = 200) String dietaryRestriction
) {
}
