package ma.femme.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.femme.dto.AssignmentResultDTO;
import ma.femme.dto.ManualAssignmentRequest;
import ma.femme.dto.TableWithGuestsDTO;
import ma.femme.service.TableAssignmentService;

@RestController
@RequestMapping("/admin/assignments")
@RequiredArgsConstructor
public class TableAssignmentController {

  private final TableAssignmentService tableAssignmentService;

  @PostMapping("/automatic")
  public AssignmentResultDTO assignAutomatically() {
    return tableAssignmentService.assignAutomatically();
  }

  @PostMapping("/manual")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void assignManually(
      @Valid @RequestBody ManualAssignmentRequest request) {
    tableAssignmentService.assignManually(request);
  }

  @DeleteMapping("/{guestId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void removeAssignment(@PathVariable Long guestId) {
    tableAssignmentService.removeAssignment(guestId);
  }

  @GetMapping("/layout")
  public List<TableWithGuestsDTO> getTableLayout() {
    return tableAssignmentService.getTableLayout();
  }
}
