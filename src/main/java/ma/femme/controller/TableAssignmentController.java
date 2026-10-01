package ma.femme.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.femme.dto.AssignmentResultDTO;
import ma.femme.dto.ManualAssignmentRequest;
import ma.femme.dto.TableWithGuestsDTO;
import ma.femme.service.TableAssignmentService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/assignments")
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