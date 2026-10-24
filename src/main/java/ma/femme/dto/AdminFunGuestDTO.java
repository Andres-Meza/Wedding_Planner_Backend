package ma.femme.dto;

import java.time.Instant;
import java.util.UUID;

import ma.femme.entity.AttendanceStatus;
import ma.femme.entity.InvitationType;

// Una fila de la administración de las dos invitaciones: un invitado con el
// enlace de la invitación formal (token de su invitación, compartido por la
// familia) y el de la divertida (token propio). Solo para /admin/**.
public record AdminFunGuestDTO(
    Long guestId,
    String fullName,
    Long invitationId,
    // Persona o familia a la que pertenece (Invitation.referenceName).
    String invitationName,
    InvitationType invitationType,
    UUID formalToken,
    UUID funToken,
    // Confirmación de asistencia de la invitación formal.
    AttendanceStatus formalAttendance,
    FunQuizState state,
    int answeredCount,
    int totalQuestions,
    Instant startedAt,
    Instant lastActivityAt,
    Instant completedAt,
    String verdictKey,
    String verdictHeadline) {
}
