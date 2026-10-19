package ma.femme.dto;

import lombok.Builder;
import ma.femme.entity.AllergyType;
import ma.femme.entity.AttendanceStatus;

@Builder
public record GuestResponseDTO(
    Long id,
    String fullName,
    boolean isMinor,
    AttendanceStatus attendance,
    AllergyType allergyType,
    String dietaryRestriction,
    MenuResponseDTO menu) {
}
