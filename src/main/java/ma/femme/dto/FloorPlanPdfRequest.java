package ma.femme.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Imagen PNG del plano tal como se ve en pantalla, en base64 (con o sin el
// prefijo "data:image/png;base64,").
public record FloorPlanPdfRequest(
    @NotBlank @Size(max = FloorPlanPdfRequest.MAX_BASE64_LENGTH) String imageBase64) {

  // Un PNG de 10 MB (límite de FloorPlanPdfService) ocupa unos 14 millones
  // de caracteres en base64. Se rechaza antes de decodificar.
  public static final int MAX_BASE64_LENGTH = 15_000_000;
}
