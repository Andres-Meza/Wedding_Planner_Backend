package ma.femme.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.femme.dto.FloorPlanElementDTO;
import ma.femme.dto.SaveFloorPlanRequest;
import ma.femme.service.FloorPlanService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/floor-plan")
@RequiredArgsConstructor
public class FloorPlanController {

  private final FloorPlanService floorPlanService;

  @GetMapping
  public List<FloorPlanElementDTO> get() {
    return floorPlanService.getPlan();
  }

  @PutMapping
  public List<FloorPlanElementDTO> save(@Valid @RequestBody SaveFloorPlanRequest request) {
    return floorPlanService.savePlan(request);
  }
}
