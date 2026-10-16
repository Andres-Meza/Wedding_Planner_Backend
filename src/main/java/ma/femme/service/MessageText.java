package ma.femme.service;

// Normaliza el texto libre que escriben los invitados. El resultado siempre
// es TEXTO PLANO: no se interpreta ni se "limpia" HTML (se guarda tal cual y
// quien lo muestre debe escaparlo; Angular lo hace con {{ }}). Aquí solo se
// quitan los caracteres de control, porque PostgreSQL rechaza el NUL (0x00)
// y el resto no tiene sentido en un mensaje.
public final class MessageText {

  private MessageText() {
  }

  // Devuelve null si no queda contenido (mensaje vacío).
  public static String normalize(String raw) {
    if (raw == null) {
      return null;
    }

    StringBuilder cleaned = new StringBuilder(raw.length());
    for (int i = 0; i < raw.length(); i++) {
      char c = raw.charAt(i);
      if (c == '\r') {
        // CRLF y CR sueltos se convierten en un solo salto de línea.
        if (i + 1 < raw.length() && raw.charAt(i + 1) == '\n') {
          continue;
        }
        cleaned.append('\n');
      } else if (c == '\n' || c == '\t' || (c >= 0x20 && c != 0x7F)) {
        cleaned.append(c);
      }
    }

    String text = cleaned.toString().strip();
    return text.isEmpty() ? null : text;
  }
}
