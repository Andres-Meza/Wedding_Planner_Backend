package ma.femme.controller;

import lombok.RequiredArgsConstructor;
import ma.femme.dto.GuestFoodRowDTO;
import ma.femme.export.FoodExcelExporter;
import ma.femme.export.FoodPdfExporter;
import ma.femme.service.FoodReportService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/food-report")
@RequiredArgsConstructor
public class FoodReportController {

  private static final MediaType XLSX =
    MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

  private final FoodReportService foodReportService;
  private final FoodExcelExporter excelExporter;
  private final FoodPdfExporter pdfExporter;

  @GetMapping
  public List<GuestFoodRowDTO> list() {
    return foodReportService.getRows();
  }

  @GetMapping("/excel")
  public ResponseEntity<byte[]> excel() {
    List<GuestFoodRowDTO> rows = foodReportService.getRows();
    byte[] body = excelExporter.export(rows, foodReportService.summarize(rows));
    return download(body, "alimentacion-invitados.xlsx", XLSX);
  }

  @GetMapping("/pdf")
  public ResponseEntity<byte[]> pdf() {
    List<GuestFoodRowDTO> rows = foodReportService.getRows();
    byte[] body = pdfExporter.export(rows, foodReportService.summarize(rows));
    return download(body, "alimentacion-invitados.pdf", MediaType.APPLICATION_PDF);
  }

  private ResponseEntity<byte[]> download(byte[] body, String filename, MediaType type) {
    return ResponseEntity.ok()
      .contentType(type)
      .header(HttpHeaders.CONTENT_DISPOSITION,
        ContentDisposition.attachment().filename(filename).build().toString())
      .body(body);
  }
}
