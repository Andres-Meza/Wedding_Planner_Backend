package ma.femme.export;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

// PDF del plano: primera página con el dibujo del salón y, después, quién se
// sienta en cada mesa (con menú y alergias) para el catering y el equipo.
@Component
public class FloorPlanPdfExporter {

  public record GuestLine(String seat, String name, String menu, String allergy) {
  }

  public record TableBlock(String name, int capacity, List<GuestLine> guests) {
  }

  public record UnseatedLine(String name, String menu, String allergy) {
  }

  private static final Color ALERT_RED = new Color(155, 44, 44);
  private static final Color ALERT_BG = new Color(253, 236, 236);
  private static final Color HEADER_BG = new Color(240, 230, 210);
  private static final Color TEXT_GREY = new Color(90, 90, 90);

  private static final Font TITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
  private static final Font SECTION = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13);
  private static final Font BOLD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
  private static final Font NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 10);
  private static final Font SMALL = FontFactory.getFont(FontFactory.HELVETICA, 9, Font.NORMAL, TEXT_GREY);
  private static final Font ALERT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Font.BOLD, ALERT_RED);

  public byte[] export(byte[] planImage, List<TableBlock> tables, List<UnseatedLine> unseated) {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    // Horizontal: el plano del salón casi siempre es más ancho que alto.
    Document document = new Document(PageSize.A4.rotate(), 36, 36, 36, 46);

    try {
      PdfWriter.getInstance(document, out);

      HeaderFooter footer = new HeaderFooter(new Phrase("Página ", SMALL), true);
      footer.setBorder(Rectangle.NO_BORDER);
      footer.setAlignment(Element.ALIGN_CENTER);
      document.setFooter(footer);

      document.open();
      addPlan(document, planImage);
      addTables(document, tables);
      addUnseated(document, unseated);
    } catch (DocumentException | IOException e) {
      throw new IllegalStateException("No se pudo generar el PDF del plano", e);
    } finally {
      if (document.isOpen()) {
        document.close();
      }
    }

    return out.toByteArray();
  }

  private void addPlan(Document document, byte[] planImage) throws DocumentException, IOException {
    document.add(new Paragraph("Plano del salón", TITLE));

    Paragraph date = new Paragraph(
      "Generado el " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), SMALL);
    date.setSpacingAfter(10);
    document.add(date);

    Image image = Image.getInstance(planImage);
    // Cabe en lo que queda de la página tras el título.
    image.scaleToFit(
      document.getPageSize().getWidth() - 72,
      document.getPageSize().getHeight() - 130);
    image.setAlignment(Element.ALIGN_CENTER);
    document.add(image);
  }

  private void addTables(Document document, List<TableBlock> tables) throws DocumentException {
    document.newPage();
    document.add(sectionTitle("MESAS E INVITADOS"));

    if (tables.isEmpty()) {
      document.add(new Paragraph("Todavía no hay mesas en el plano.", NORMAL));
      return;
    }

    for (TableBlock block : tables) {
      Paragraph heading = new Paragraph(
        block.name() + "  —  " + block.guests().size() + " de " + block.capacity() + " sillas", BOLD);
      heading.setSpacingBefore(10);
      heading.setSpacingAfter(3);
      document.add(heading);

      if (block.guests().isEmpty()) {
        document.add(new Paragraph("Sin invitados asignados.", SMALL));
        continue;
      }

      PdfPTable table = new PdfPTable(new float[]{1f, 4f, 3f, 3f});
      table.setWidthPercentage(100);
      table.setHeaderRows(1);
      for (String title : new String[]{"Silla", "Invitado", "Menú", "Alergia"}) {
        table.addCell(headerCell(title));
      }

      for (GuestLine line : block.guests()) {
        boolean hasAllergy = !line.allergy().isEmpty();
        table.addCell(bodyCell(line.seat(), NORMAL, hasAllergy));
        table.addCell(bodyCell(line.name(), hasAllergy ? ALERT : NORMAL, hasAllergy));
        table.addCell(bodyCell(line.menu(), NORMAL, hasAllergy));
        table.addCell(bodyCell(hasAllergy ? line.allergy() : "-", hasAllergy ? ALERT : NORMAL, hasAllergy));
      }
      document.add(table);
    }
  }

  private void addUnseated(Document document, List<UnseatedLine> unseated) throws DocumentException {
    if (unseated.isEmpty()) {
      return;
    }

    Paragraph title = sectionTitle("CONFIRMADOS SIN MESA (" + unseated.size() + ")");
    title.setSpacingBefore(18);
    document.add(title);

    PdfPTable table = new PdfPTable(new float[]{5f, 3f, 3f});
    table.setWidthPercentage(100);
    table.setHeaderRows(1);
    for (String header : new String[]{"Invitado", "Menú", "Alergia"}) {
      table.addCell(headerCell(header));
    }
    for (UnseatedLine line : unseated) {
      boolean hasAllergy = !line.allergy().isEmpty();
      table.addCell(bodyCell(line.name(), hasAllergy ? ALERT : NORMAL, hasAllergy));
      table.addCell(bodyCell(line.menu(), NORMAL, hasAllergy));
      table.addCell(bodyCell(hasAllergy ? line.allergy() : "-", hasAllergy ? ALERT : NORMAL, hasAllergy));
    }
    document.add(table);
  }

  private Paragraph sectionTitle(String text) {
    Paragraph paragraph = new Paragraph(text, SECTION);
    paragraph.setSpacingAfter(4);
    return paragraph;
  }

  private PdfPCell headerCell(String text) {
    PdfPCell cell = new PdfPCell(new Phrase(text, BOLD));
    cell.setBackgroundColor(HEADER_BG);
    cell.setPadding(4);
    return cell;
  }

  private PdfPCell bodyCell(String text, Font font, boolean highlight) {
    PdfPCell cell = new PdfPCell(new Phrase(text, font));
    cell.setPadding(4);
    if (highlight) {
      cell.setBackgroundColor(ALERT_BG);
    }
    return cell;
  }
}
