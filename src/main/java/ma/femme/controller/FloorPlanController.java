package ma.femme.controller;

import java.util.List;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.femme.dto.FloorPlanElementDTO;
import ma.femme.dto.FloorPlanPdfRequest;
import ma.femme.dto.SaveFloorPlanRequest;
import ma.femme.service.FloorPlanPdfService;
import ma.femme.service.FloorPlanService;

@RestController
@RequestMapping("/admin/floor-plan")
@RequiredArgsConstructor
public class FloorPlanController {

  private final FloorPlanService floorPlanService;
  private final FloorPlanPdfService floorPlanPdfService;

  @GetMapping
  public List<FloorPlanElementDTO> get() {
    return floorPlanService.getPlan();
  }

  @PutMapping
  public List<FloorPlanElementDTO> save(@Valid @RequestBody SaveFloorPlanRequest request) {
    return floorPlanService.savePlan(request);
  }

  // Recibe el dibujo del plano (PNG en base64, generado en el navegador) y
  // devuelve el PDF: el plano + quién se sienta en cada mesa.
  @PostMapping("/pdf")
  public ResponseEntity<byte[]> pdf(@Valid @RequestBody FloorPlanPdfRequest request) {
    byte[] body = floorPlanPdfService.buildPdf(request.imageBase64());
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_PDF)
        .header(HttpHeaders.CONTENT_DISPOSITION,
            ContentDisposition.attachment().filename("plano-del-salon.pdf").build().toString())
        .body(body);
  }
}
