package ma.femme.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MessageTextTest {

  @Test
  void nullAndBlankBecomeNull() {
    assertThat(MessageText.normalize(null)).isNull();
    assertThat(MessageText.normalize("")).isNull();
    assertThat(MessageText.normalize("  \n\t ")).isNull();
  }

  @Test
  void trimsAndKeepsLineBreaksAndAccents() {
    assertThat(MessageText.normalize("  ¡Felicidades!\n\nCon cariño  "))
      .isEqualTo("¡Felicidades!\n\nCon cariño");
  }

  @Test
  void convertsWindowsAndOldMacLineBreaks() {
    assertThat(MessageText.normalize("a\r\nb\rc")).isEqualTo("a\nb\nc");
  }

  @Test
  void removesNulAndOtherControlCharacters() {
    // PostgreSQL rechaza NUL (0x00): nunca debe llegar a la base de datos.
    assertThat(MessageText.normalize("ho\u0000la\u0007\u007F")).isEqualTo("hola");
  }

  @Test
  void keepsHtmlAsInertPlainText() {
    String html = "<script>alert('x')</script> <b>hola</b>";
    assertThat(MessageText.normalize(html)).isEqualTo(html);
  }
}
