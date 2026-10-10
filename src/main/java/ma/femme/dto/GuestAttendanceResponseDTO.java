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
  @Size(max = 200) String dietaryRestriction,
  // Obligatorio solo cuando attendance = CONFIRMED; se valida en el
  // servicio (que exista, que esté activo y que sea de la categoría
  // del invitado: adulto o infantil).
  Long menuId
) {
}
