package ma.femme.dto;

import jakarta.validation.constraints.Size;
import ma.femme.entity.Invitation;

// Mensaje de los invitados para los novios. Es opcional: null o texto en
// blanco borra el mensaje guardado. Se trata siempre como texto plano.
public record CoupleMessageRequest(
    @Size(max = Invitation.COUPLE_MESSAGE_MAX_LENGTH, message = "el mensaje no puede superar los "
        + Invitation.COUPLE_MESSAGE_MAX_LENGTH + " caracteres") String message) {
}
