package ma.femme.export;

import ma.femme.dto.GuestFoodRowDTO;
import ma.femme.service.FoodReportService.AllergyCount;
import ma.femme.service.FoodReportService.FoodSummary;
import ma.femme.service.FoodReportService.MenuCount;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

@Component
public class FoodExcelExporter {

  // Columnas de la hoja de invitados. El plato fuerte ocupa cuatro columnas
  // consecutivas (de MAIN_COURSE_FIRST a MAIN_COURSE_LAST) que se agrupan
  // bajo un encabezado combinado "Plato fuerte".
  private static final String[] HEADERS = {
          "Invitado", "Tipo", "Asistencia", "Menú", "Entrada",
          "Proteína 1", "Proteína 2", "Acompañamiento", "Ensalada",
          "Postre", "Bebida", "Tiene alergia", "Alergia", "Observaciones"
  };

  private static final int MAIN_COURSE_FIRST = 5;
  private static final int MAIN_COURSE_LAST = 8;
  private static final String MAIN_COURSE_TITLE = "Plato fuerte";

  // Anchos en caracteres. Fijos a propósito: autoSizeColumn necesita fuentes
  // del sistema (AWT) y falla en contenedores mínimos como el de Railway.
  private static final int[] WIDTHS = {28, 10, 15, 18, 24, 22, 22, 20, 20, 20, 18, 14, 16, 40};

  public byte[] export(List<GuestFoodRowDTO> rows, FoodSummary summary) {
    try (XSSFWorkbook workbook = new XSSFWorkbook();
         ByteArrayOutputStream out = new ByteArrayOutputStream()) {

      Styles styles = new Styles(workbook);
      buildGuestSheet(workbook, styles, rows);
      buildSummarySheet(workbook, styles, summary);

      workbook.write(out);
      return out.toByteArray();
    } catch (IOException e) {
      throw new UncheckedIOException("No se pudo generar el archivo de Excel", e);
    }
  }

  private void buildGuestSheet(XSSFWorkbook workbook, Styles styles, List<GuestFoodRowDTO> rows) {
    Sheet sheet = workbook.createSheet("Invitados");

    // Encabezado de dos filas: arriba el grupo "Plato fuerte" (combinado sobre
    // sus cuatro columnas) y abajo los nombres de cada columna. Las columnas
    // que no pertenecen al plato fuerte combinan ambas filas.
    Row groupRow = sheet.createRow(0);
    Row header = sheet.createRow(1);

    for (int i = 0; i < HEADERS.length; i++) {
      sheet.setColumnWidth(i, WIDTHS[i] * 256);

      if (isMainCourseColumn(i)) {
        header.createCell(i).setCellValue(HEADERS[i]);
        header.getCell(i).setCellStyle(styles.header);
        groupRow.createCell(i).setCellStyle(styles.header);
      } else {
        groupRow.createCell(i).setCellValue(HEADERS[i]);
        groupRow.getCell(i).setCellStyle(styles.header);
        header.createCell(i).setCellStyle(styles.header);
        sheet.addMergedRegion(new CellRangeAddress(0, 1, i, i));
      }
    }

    groupRow.getCell(MAIN_COURSE_FIRST).setCellValue(MAIN_COURSE_TITLE);
    sheet.addMergedRegion(new CellRangeAddress(0, 0, MAIN_COURSE_FIRST, MAIN_COURSE_LAST));

    int rowIndex = 2;
    for (GuestFoodRowDTO guest : rows) {
      Row row = sheet.createRow(rowIndex++);
      boolean hasAllergy = guest.allergyType() != null;
      var menu = guest.menu();
      var mainCourse = menu == null ? null : menu.mainCourse();

      String[] values = {
              guest.guestName(),
              ReportLabels.guestType(guest.isMinor()),
              ReportLabels.attendance(guest.attendance()),
              menu == null ? "" : menu.name(),
              menu == null ? "" : menu.starter(),
              mainCourse == null ? "" : ReportLabels.text(mainCourse.protein1()),
              mainCourse == null ? "" : ReportLabels.text(mainCourse.protein2()),
              mainCourse == null ? "" : ReportLabels.text(mainCourse.accompaniment()),
              mainCourse == null ? "" : ReportLabels.text(mainCourse.salad()),
              menu == null ? "" : menu.dessert(),
              menu == null ? "" : menu.drink(),
              hasAllergy ? "Sí" : "No",
              ReportLabels.allergy(guest),
              ReportLabels.observations(guest)
      };

      for (int i = 0; i < values.length; i++) {
        row.createCell(i).setCellValue(values[i]);
        // Las filas con alergia se resaltan para que no pasen desapercibidas.
        row.getCell(i).setCellStyle(hasAllergy ? styles.allergyRow : styles.normal);
      }
    }

    sheet.createFreezePane(0, 2);
    // El filtro va sobre la segunda fila del encabezado.
    sheet.setAutoFilter(new CellRangeAddress(1, Math.max(rowIndex - 1, 1), 0, HEADERS.length - 1));
  }

  private static boolean isMainCourseColumn(int column) {
    return column >= MAIN_COURSE_FIRST && column <= MAIN_COURSE_LAST;
  }

  private void buildSummarySheet(XSSFWorkbook workbook, Styles styles, FoodSummary summary) {
    Sheet sheet = workbook.createSheet("Resumen");
    sheet.setColumnWidth(0, 34 * 256);
    sheet.setColumnWidth(1, 20 * 256);

    int r = 0;

    r = section(sheet, styles, r, "RESUMEN GENERAL (confirmados)");
    r = line(sheet, styles, r, "Invitados confirmados", String.valueOf(summary.totalConfirmed()));
    r = line(sheet, styles, r, "Adultos", String.valueOf(summary.totalAdults()));
    r = line(sheet, styles, r, "Niños", String.valueOf(summary.totalKids()));
    r++;

    r = section(sheet, styles, r, "RESUMEN DE MENÚS");
    long total = 0;
    for (MenuCount menu : summary.menuCounts()) {
      r = line(sheet, styles, r, menu.menuName(),
              ReportLabels.peopleCount(menu.count(), menu.isKids()));
      total += menu.count();
    }
    r = line(sheet, styles, r, "TOTAL", ReportLabels.peopleCount(total, false));
    r++;

    r = section(sheet, styles, r, "ALERGIAS");
    if (summary.allergyCounts().isEmpty()) {
      r = line(sheet, styles, r, "Sin alergias reportadas", "");
    }
    for (AllergyCount allergy : summary.allergyCounts()) {
      r = line(sheet, styles, r, allergy.type().getLabel(), String.valueOf(allergy.count()));
    }
    line(sheet, styles, r, "TOTAL con alergias", String.valueOf(summary.totalWithAllergies()));
  }

  private int section(Sheet sheet, Styles styles, int rowIndex, String title) {
    Row row = sheet.createRow(rowIndex);
    row.createCell(0).setCellValue(title);
    row.getCell(0).setCellStyle(styles.header);
    row.createCell(1).setCellStyle(styles.header);
    return rowIndex + 1;
  }

  private int line(Sheet sheet, Styles styles, int rowIndex, String label, String value) {
    Row row = sheet.createRow(rowIndex);
    row.createCell(0).setCellValue(label);
    row.createCell(1).setCellValue(value);
    row.getCell(0).setCellStyle(styles.normal);
    row.getCell(1).setCellStyle(styles.normal);
    return rowIndex + 1;
  }

  private static final class Styles {
    final CellStyle header;
    final CellStyle normal;
    final CellStyle allergyRow;

    Styles(XSSFWorkbook workbook) {
      Font bold = workbook.createFont();
      bold.setBold(true);

      header = workbook.createCellStyle();
      header.setFont(bold);
      header.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
      header.setFillPattern(FillPatternType.SOLID_FOREGROUND);

      normal = workbook.createCellStyle();
      normal.setWrapText(true);
      normal.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.TOP);

      allergyRow = workbook.createCellStyle();
      allergyRow.cloneStyleFrom(normal);
      allergyRow.setFillForegroundColor(IndexedColors.ROSE.getIndex());
      allergyRow.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    }
  }
}
