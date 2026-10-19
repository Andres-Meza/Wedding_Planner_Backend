package ma.femme.dto;

import java.time.Instant;

import ma.femme.entity.Invitation;
import ma.femme.entity.InvitationStatus;
import ma.femme.entity.InvitationType;

// Fila de la vista administrativa de mensajes. A propósito NO incluye el
// token ni datos de contacto: esa información no hace falta para leer los
// mensajes y el enlace privado se administra solo en la pantalla de
// invitaciones.
public record GuestMessageDTO(
    Long invitationId,
    // Nombre de la persona o familia (Invitation.referenceName).
    String referenceName,
    InvitationType type,
    int guestCount,
    // null si la invitación todavía no dejó mensaje.
    String message,
    Instant messageUpdatedAt,
    // Estado de la respuesta de asistencia de esa invitación.
    InvitationStatus invitationStatus) {

  public static GuestMessageDTO fromEntity(Invitation invitation) {
    return new GuestMessageDTO(
        invitation.getId(),
        invitation.getReferenceName(),
        invitation.getType(),
        invitation.getGuests().size(),
        invitation.getCoupleMessage(),
        invitation.coupleMessageUpdatedAtInstant(),
        invitation.getStatus());
  }
}
