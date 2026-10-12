package ma.femme.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SaveFloorPlanRequest(
  @NotNull @Valid List<FloorPlanElementDTO> elements
) {
}
