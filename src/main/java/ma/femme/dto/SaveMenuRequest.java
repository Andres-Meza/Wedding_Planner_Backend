package ma.femme.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Se usa tanto para crear como para editar un menú.
public record SaveMenuRequest(
  @NotBlank @Size(max = 100) String name,
  @NotBlank @Size(max = 200) String starter,
  @NotBlank @Size(max = 200) String mainCourse,
  @NotBlank @Size(max = 200) String dessert,
  @NotBlank @Size(max = 200) String drink,
  boolean isKids
) {
}
