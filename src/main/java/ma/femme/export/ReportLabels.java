package ma.femme.export;

import ma.femme.dto.GuestFoodRowDTO;
import ma.femme.entity.AttendanceStatus;

// Textos en español compartidos por los exportadores de Excel y PDF.
final class ReportLabels {

  private ReportLabels() {
  }

  static String guestType(boolean isMinor) {
    return isMinor ? "Niño" : "Adulto";
  }

  static String attendance(AttendanceStatus status) {
    return switch (status) {
      case CONFIRMED -> "Confirmado";
      case NOT_ATTENDING -> "No asiste";
      case NO_RESPONSE -> "Sin respuesta";
    };
  }

  // Los componentes opcionales del plato fuerte pueden venir en null.
  static String text(String value) {
    return value == null ? "" : value;
  }

  static String allergy(GuestFoodRowDTO row) {
    return row.allergyType() == null ? "" : row.allergyType().getLabel();
  }

  static String observations(GuestFoodRowDTO row) {
    return row.dietaryRestriction() == null ? "" : row.dietaryRestriction().trim();
  }

  static boolean needsAttention(GuestFoodRowDTO row) {
    return row.allergyType() != null || !observations(row).isEmpty();
  }

  static String peopleCount(long count, boolean isKids) {
    if (isKids) {
      return count + (count == 1 ? " niño" : " niños");
    }
    return count + (count == 1 ? " persona" : " personas");
  }
}
