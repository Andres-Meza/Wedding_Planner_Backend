package ma.femme.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// El servidor valida además que la opción exista para esa pregunta; aquí solo
// se acota la forma del dato antes de llegar al servicio.
public record FunAnswerRequest(
    @NotBlank(message = "la opción es obligatoria")
    @Size(max = 40, message = "la opción no es válida") String optionKey) {
}
