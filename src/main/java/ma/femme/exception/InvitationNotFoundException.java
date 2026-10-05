package ma.femme.exception;

import java.util.UUID;

public class InvitationNotFoundException extends RuntimeException {

    public InvitationNotFoundException(UUID token) {
        super("No invitation exists with the token: " + token);
    }
}
