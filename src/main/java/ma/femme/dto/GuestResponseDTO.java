package ma.femme.dto;

import lombok.Builder;
import ma.femme.entity.AllergyType;
import ma.femme.entity.AttendanceStatus;
import ma.femme.entity.Guest;

@Builder
public record GuestResponseDTO(
    Long id,
    String fullName,
    boolean isMinor,
    AttendanceStatus attendance,
    AllergyType allergyType,
    String dietaryRestriction,
    MenuResponseDTO menu) {

  public static GuestResponseDTO fromEntity(Guest guest) {
    return GuestResponseDTO.builder()
        .id(guest.getId())
        .fullName(guest.getFullName())
        .isMinor(guest.isMinor())
        .attendance(guest.getAttendance())
        .allergyType(guest.getAllergyType())
        .dietaryRestriction(guest.getDietaryRestriction())
        .menu(MenuResponseDTO.fromEntity(guest.getMenu()))
        .build();
  }
}
