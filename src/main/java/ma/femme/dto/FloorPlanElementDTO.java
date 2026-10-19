package ma.femme.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import ma.femme.entity.FloorPlanElementKind;

// Sirve de respuesta y de petición. id == null significa "pieza nueva".
// Los rangos de x, y, width, height y rotation se validan en FloorPlanService
// (Bean Validation no cubre bien los números decimales).
@Builder
public record FloorPlanElementDTO(
    Long id,
    @NotNull FloorPlanElementKind kind,
    double x,
    double y,
    double width,
    double height,
    double rotation,
    @Size(max = 100) String label,
    Long tableId) {
}
