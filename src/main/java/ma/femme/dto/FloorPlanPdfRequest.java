package ma.femme.dto;

import jakarta.validation.constraints.NotBlank;

// Imagen PNG del plano tal como se ve en pantalla, en base64 (con o sin el
// prefijo "data:image/png;base64,").
public record FloorPlanPdfRequest(
    @NotBlank String imageBase64) {
}
