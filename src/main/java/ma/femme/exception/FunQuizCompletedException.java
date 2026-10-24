package ma.femme.exception;

// Se intentó cambiar una respuesta de un cuestionario que ya se completó: el
// resultado es definitivo y no debe cambiar al volver a abrir el enlace.
public class FunQuizCompletedException extends RuntimeException {

  public FunQuizCompletedException() {
    super("The quiz is already completed; its answers can no longer be changed");
  }
}
