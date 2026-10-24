package ma.femme.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

// Lo que ve el invitado por su enlace individual. A propósito NO incluye el
// token (ni el de la invitación formal) ni datos de otros invitados de su
// familia: solo su nombre, las preguntas, SUS respuestas y su veredicto.
public record FunInvitationDTO(
    String guestName,
    FunQuizState state,
    List<FunQuestionDTO> questions,
    // clave de pregunta -> clave de opción elegida (solo las ya respondidas).
    Map<String, String> answers,
    // null hasta que el cuestionario se completa.
    FunVerdictDTO verdict,
    Instant completedAt) {
}
