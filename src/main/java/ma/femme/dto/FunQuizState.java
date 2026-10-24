package ma.femme.dto;

// Estado del cuestionario de un invitado. Son excluyentes: cada invitado está
// en exactamente uno. NOT_STARTED = todavía no guardó ninguna respuesta.
public enum FunQuizState {
  NOT_STARTED,
  IN_PROGRESS,
  COMPLETED
}
