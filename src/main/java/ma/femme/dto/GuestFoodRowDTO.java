package ma.femme.dto;

import lombok.Builder;
import ma.femme.entity.AllergyType;
import ma.femme.entity.AttendanceStatus;

// Una fila del reporte de alimentación: un invitado con su menú y su
// información de alergias. allergyType == null significa "sin alergias".
@Builder
public record GuestFoodRowDTO(
    Long guestId,
    String guestName,
    String invitationName,
    boolean isMinor,
    AttendanceStatus attendance,
    MenuResponseDTO menu,
    AllergyType allergyType,
    String dietaryRestriction) {
}
