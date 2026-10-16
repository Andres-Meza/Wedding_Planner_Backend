package ma.femme.exception;

// El menú por defecto (Menú 1 / Menú Infantil 1) no existe o está inactivo.
// Se informa en vez de guardar una asignación inválida o elegir otro menú.
public class DefaultMenuUnavailableException extends RuntimeException {

  public DefaultMenuUnavailableException(String message) {
    super(message);
  }
}
