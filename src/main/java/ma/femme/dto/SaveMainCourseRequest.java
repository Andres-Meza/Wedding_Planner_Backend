package ma.femme.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Plato fuerte dentro de SaveMenuRequest. Solo la proteína 1 es obligatoria
// aquí; que un menú de adultos exija los cuatro componentes lo valida
// MenuService, porque depende de "isKids".
public record SaveMainCourseRequest(
    @NotBlank @Size(max = 200) String protein1,
    @Size(max = 200) String protein2,
    @Size(max = 200) String accompaniment,
    @Size(max = 200) String salad) {
}
