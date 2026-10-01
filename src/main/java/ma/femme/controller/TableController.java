package ma.femme.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.femme.dto.CreateTableRequest;
import ma.femme.dto.TableResponseDTO;
import ma.femme.service.TableService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/tables")
@RequiredArgsConstructor
public class TableController {

    private final TableService tableService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TableResponseDTO create(@Valid @RequestBody CreateTableRequest request) {
        return tableService.create(request);
    }

    @GetMapping
    public List<TableResponseDTO> list() {
        return tableService.listAll();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        tableService.delete(id);
    }
}