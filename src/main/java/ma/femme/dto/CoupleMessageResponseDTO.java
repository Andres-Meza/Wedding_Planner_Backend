package ma.femme.dto;

import java.time.Instant;

// Lo que quedó guardado (ya normalizado). message = null si no hay mensaje.
public record CoupleMessageResponseDTO(
  String message,
  Instant updatedAt
) {
}
