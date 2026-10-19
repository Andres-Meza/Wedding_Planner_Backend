package ma.femme.exception;

public class MenuInUseException extends RuntimeException {

  public MenuInUseException(String menuName) {
    super("No puedes eliminar el menú '" + menuName
        + "' porque ya tiene invitados asociados. Puedes desactivarlo en su lugar.");
  }
}
