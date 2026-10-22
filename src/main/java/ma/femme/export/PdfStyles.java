package ma.femme.export;

import java.awt.Color;

import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;

// Colores y tipografías compartidos por los PDF para que se vean igual.
final class PdfStyles {

  static final Color ALERT_RED = new Color(155, 44, 44);
  static final Color ALERT_BG = new Color(253, 236, 236);
  static final Color HEADER_BG = new Color(240, 230, 210);
  static final Color TEXT_GREY = new Color(90, 90, 90);

  static final Font TITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
  static final Font SECTION = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13);
  static final Font BOLD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
  static final Font NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 10);
  static final Font SMALL = FontFactory.getFont(FontFactory.HELVETICA, 9, Font.NORMAL, TEXT_GREY);
  static final Font ALERT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Font.BOLD, ALERT_RED);

  private PdfStyles() {
  }
}
