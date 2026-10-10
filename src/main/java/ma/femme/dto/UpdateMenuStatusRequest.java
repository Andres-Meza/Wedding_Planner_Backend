package ma.femme.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateMenuStatusRequest(
  @NotNull Boolean active
) {
}
