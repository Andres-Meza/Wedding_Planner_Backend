package ma.femme.export;

import static ma.femme.export.PdfStyles.ALERT;
import static ma.femme.export.PdfStyles.ALERT_BG;
import static ma.femme.export.PdfStyles.ALERT_RED;
import static ma.femme.export.PdfStyles.BOLD;
import static ma.femme.export.PdfStyles.HEADER_BG;
import static ma.femme.export.PdfStyles.NORMAL;
import static ma.femme.export.PdfStyles.SECTION;
import static ma.femme.export.PdfStyles.SMALL;
import static ma.femme.export.PdfStyles.TITLE;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Component;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import ma.femme.dto.GuestFoodRowDTO;
import ma.femme.dto.MainCourseDTO;
import ma.femme.entity.AttendanceStatus;
import ma.femme.service.FoodReportService.FoodSummary;
import ma.femme.service.FoodReportService.MenuCount;

// PDF pensado para entregar al catering: resumen, alergias primero (lo más
// crítico) y luego el detalle de cada invitado confirmado.
@Component
public class FoodPdfExporter {

  private static final Font ALERT_HEADER = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Font.BOLD, Color.WHITE);

  public byte[] export(List<GuestFoodRowDTO> rows, FoodSummary summary) {
    // Al catering solo le interesa quien realmente asiste.
    List<GuestFoodRowDTO> confirmed = rows.stream()
        .filter(row -> row.attendance() == AttendanceStatus.CONFIRMED)
        .toList();

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Document document = new Document(PageSize.A4, 40, 40, 40, 50);

    try {
      PdfWriter.getInstance(document, out);

      HeaderFooter footer = new HeaderFooter(new Phrase("Página ", SMALL), true);
      footer.setBorder(Rectangle.NO_BORDER);
      footer.setAlignment(Element.ALIGN_CENTER);
      document.setFooter(footer);

      document.open();
      addTitle(document);
      addSummary(document, summary);
      addAllergySection(document, confirmed);
      addGuestDetail(document, confirmed);
    } catch (DocumentException e) {
      throw new IllegalStateException("No se pudo generar el PDF", e);
    } finally {
      if (document.isOpen()) {
        document.close();
      }
    }

    return out.toByteArray();
  }

  private void addTitle(Document document) throws DocumentException {
    document.add(new Paragraph("Alimentación de invitados", TITLE));
    Paragraph date = new Paragraph(
        "Generado el " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), SMALL);
    date.setSpacingAfter(14);
    document.add(date);
  }

  private void addSummary(Document document, FoodSummary summary) throws DocumentException {
    document.add(sectionTitle("RESUMEN GENERAL"));

    PdfPTable general = new PdfPTable(2);
    general.setWidthPercentage(60);
    general.setHorizontalAlignment(Element.ALIGN_LEFT);
    summaryRow(general, "Invitados confirmados", String.valueOf(summary.totalConfirmed()));
    summaryRow(general, "Adultos", String.valueOf(summary.totalAdults()));
    summaryRow(general, "Niños", String.valueOf(summary.totalKids()));
    summaryRow(general, "Personas con alergias", String.valueOf(summary.totalWithAllergies()));
    document.add(general);

    Paragraph byMenu = new Paragraph("Total por menú", BOLD);
    byMenu.setSpacingBefore(10);
    byMenu.setSpacingAfter(4);
    document.add(byMenu);

    PdfPTable menus = new PdfPTable(2);
    menus.setWidthPercentage(60);
    menus.setHorizontalAlignment(Element.ALIGN_LEFT);
    for (MenuCount menu : summary.menuCounts()) {
      summaryRow(menus, menu.menuName(), ReportLabels.peopleCount(menu.count(), menu.isKids()));
    }
    if (summary.menuCounts().isEmpty()) {
      summaryRow(menus, "Sin menús elegidos", "");
    }
    document.add(menus);
  }

  private void addAllergySection(Document document, List<GuestFoodRowDTO> confirmed)
      throws DocumentException {

    document.add(spacedSectionTitle("ALERGIAS Y RESTRICCIONES ALIMENTARIAS"));

    List<GuestFoodRowDTO> flagged = confirmed.stream()
        .filter(ReportLabels::needsAttention)
        .toList();

    if (flagged.isEmpty()) {
      document.add(new Paragraph("Sin alergias ni restricciones reportadas.", NORMAL));
      return;
    }

    PdfPTable table = new PdfPTable(new float[] { 3f, 2f, 2f, 4f });
    table.setWidthPercentage(100);
    table.setHeaderRows(1);

    for (String title : new String[] { "Invitado", "Menú", "Alergia", "Observaciones" }) {
      PdfPCell cell = new PdfPCell(new Phrase(title, ALERT_HEADER));
      cell.setBackgroundColor(ALERT_RED);
      cell.setPadding(5);
      table.addCell(cell);
    }

    for (GuestFoodRowDTO row : flagged) {
      boolean hasAllergy = row.allergyType() != null;
      Font font = hasAllergy ? ALERT : NORMAL;

      table.addCell(alertCell(row.guestName(), font));
      table.addCell(alertCell(row.menu() == null ? "-" : row.menu().name(), NORMAL));
      table.addCell(alertCell(hasAllergy ? ReportLabels.allergy(row) : "Restricción", font));
      table.addCell(alertCell(ReportLabels.observations(row), NORMAL));
    }

    document.add(table);
  }

  private void addGuestDetail(Document document, List<GuestFoodRowDTO> confirmed)
      throws DocumentException {

    document.add(spacedSectionTitle("INVITADOS Y MENÚ"));

    if (confirmed.isEmpty()) {
      document.add(new Paragraph("Todavía no hay invitados confirmados.", NORMAL));
      return;
    }

    PdfPTable table = new PdfPTable(1);
    table.setWidthPercentage(100);

    for (GuestFoodRowDTO row : confirmed) {
      boolean hasAllergy = row.allergyType() != null;

      PdfPCell cell = new PdfPCell();
      cell.setPadding(6);
      cell.setBorder(Rectangle.BOTTOM);
      cell.setBorderColor(Color.LIGHT_GRAY);
      if (hasAllergy) {
        cell.setBackgroundColor(ALERT_BG);
      }

      cell.addElement(new Paragraph(
          row.guestName() + "  (" + ReportLabels.guestType(row.isMinor()) + ")", BOLD));

      var menu = row.menu();
      if (menu == null) {
        cell.addElement(new Paragraph("Menú: sin elegir", NORMAL));
      } else {
        cell.addElement(new Paragraph("Menú: " + menu.name(), NORMAL));
        cell.addElement(new Paragraph("Entrada: " + menu.starter(), NORMAL));
        addMainCourse(cell, menu.mainCourse());
        cell.addElement(new Paragraph("Postre: " + menu.dessert(), NORMAL));
        cell.addElement(new Paragraph("Bebida: " + menu.drink(), NORMAL));
      }

      if (hasAllergy) {
        cell.addElement(new Paragraph("ALERGIA: " + ReportLabels.allergy(row), ALERT));
      } else {
        cell.addElement(new Paragraph("Alergias: Ninguna", NORMAL));
      }

      String observations = ReportLabels.observations(row);
      if (!observations.isEmpty()) {
        cell.addElement(new Paragraph("Observaciones: " + observations,
            hasAllergy ? ALERT : NORMAL));
      }

      table.addCell(cell);
    }

    document.add(table);
  }

  // "Plato fuerte" como sección, con sus componentes sangrados debajo para que
  // no se confundan con la entrada, el postre o la bebida. Los componentes
  // opcionales que no se definieron (menús infantiles) se omiten.
  private void addMainCourse(PdfPCell cell, MainCourseDTO mainCourse) {
    cell.addElement(new Paragraph("Plato fuerte:", BOLD));

    if (mainCourse == null) {
      return;
    }

    addMainCourseLine(cell, "Proteína 1", mainCourse.protein1());
    addMainCourseLine(cell, "Proteína 2", mainCourse.protein2());
    addMainCourseLine(cell, "Acompañamiento", mainCourse.accompaniment());
    addMainCourseLine(cell, "Ensalada", mainCourse.salad());
  }

  private void addMainCourseLine(PdfPCell cell, String label, String value) {
    if (value == null || value.isBlank()) {
      return;
    }
    Paragraph line = new Paragraph("- " + label + ": " + value, NORMAL);
    line.setIndentationLeft(14);
    cell.addElement(line);
  }

  private Paragraph sectionTitle(String text) {
    Paragraph paragraph = new Paragraph(text, SECTION);
    paragraph.setSpacingAfter(6);
    return paragraph;
  }

  private Paragraph spacedSectionTitle(String text) {
    Paragraph paragraph = sectionTitle(text);
    paragraph.setSpacingBefore(18);
    return paragraph;
  }

  private void summaryRow(PdfPTable table, String label, String value) {
    PdfPCell labelCell = new PdfPCell(new Phrase(label, NORMAL));
    labelCell.setBackgroundColor(HEADER_BG);
    labelCell.setPadding(4);
    table.addCell(labelCell);

    PdfPCell valueCell = new PdfPCell(new Phrase(value, BOLD));
    valueCell.setPadding(4);
    table.addCell(valueCell);
  }

  private PdfPCell alertCell(String text, Font font) {
    PdfPCell cell = new PdfPCell(new Phrase(text, font));
    cell.setPadding(5);
    cell.setBackgroundColor(ALERT_BG);
    return cell;
  }
}