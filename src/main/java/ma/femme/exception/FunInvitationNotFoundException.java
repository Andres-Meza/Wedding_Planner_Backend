package ma.femme.exception;

// El token de la invitación divertida no corresponde a ningún invitado. El
// mensaje no incluye el token recibido (no se repite una credencial en las
// respuestas ni en los registros).
public class FunInvitationNotFoundException extends RuntimeException {

  public FunInvitationNotFoundException() {
    super("No fun invitation exists for this link");
  }
}
