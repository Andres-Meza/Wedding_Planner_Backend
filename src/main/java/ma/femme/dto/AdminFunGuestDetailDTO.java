package ma.femme.dto;

import java.util.List;

public record AdminFunGuestDetailDTO(
    AdminFunGuestDTO guest,
    // En el orden del catálogo; solo las preguntas ya respondidas.
    List<AdminFunAnswerDTO> answers,
    // null si el cuestionario todavía no está completado.
    FunVerdictDTO verdict) {
}
