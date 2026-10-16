package ma.femme.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(InvitationNotFoundException.class)
  public ResponseEntity<Map<String, Object>> handleInvitationNotFound(
      InvitationNotFoundException ex) {
    return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
  }

  // Borrar un menú que ya eligieron invitados.
  @ExceptionHandler(MenuInUseException.class)
  public ResponseEntity<Map<String, Object>> handleMenuInUse(MenuInUseException ex) {
    return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
  }

  // El menú por defecto (Menú 1 / Menú Infantil 1) no existe o está inactivo.
  @ExceptionHandler(DefaultMenuUnavailableException.class)
  public ResponseEntity<Map<String, Object>> handleDefaultMenuUnavailable(
      DefaultMenuUnavailableException ex) {
    return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, Object>> handleInvalidArgument(
      IllegalArgumentException ex) {
    return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
  }

  // Se dispara, por ejemplo, al borrar una mesa que todavía tiene
  // invitados asignados (la FK tiene ON DELETE RESTRICT).
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation(
      DataIntegrityViolationException ex) {
    return buildResponse(
        HttpStatus.CONFLICT,
        "This action violates a data integrity constraint — check for related records "
            + "(e.g. a table still has guests assigned) before trying again.");
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidation(
      MethodArgumentNotValidException ex) {

    String detail = ex.getBindingResult().getFieldErrors().stream()
        .map(error -> error.getField() + ": " + error.getDefaultMessage())
        .reduce((a, b) -> a + "; " + b)
        .orElse("Invalid data");

    return buildResponse(HttpStatus.BAD_REQUEST, detail);
  }

  private ResponseEntity<Map<String, Object>> buildResponse(
      HttpStatus status,
      String message) {

    Map<String, Object> body = Map.of(
        "timestamp", LocalDateTime.now().toString(),
        "status", status.value(),
        "message", message
    );

    return ResponseEntity.status(status).body(body);
  }
}
