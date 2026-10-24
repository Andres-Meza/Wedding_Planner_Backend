package ma.femme.exception;

public class GuestNotFoundException extends RuntimeException {

  public GuestNotFoundException(Long guestId) {
    super("No guest exists with id " + guestId);
  }
}
