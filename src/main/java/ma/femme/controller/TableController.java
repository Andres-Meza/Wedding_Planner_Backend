package ma.femme.controller;

import ma.femme.dto.CreateTableRequest;
import ma.femme.dto.TableResponseDTO;
import ma.femme.service.TableService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/tables")
@RequiredArgsConstructor
public class TableController {

    private final TableService tableService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TableResponseDTO create(@Valid @RequestBody CreateTableRequest request) {
        return tableService.create(request);
    }

    @PutMapping("/{id}")
    public TableResponseDTO update(
            @PathVariable Long id,
            @Valid @RequestBody CreateTableRequest request) {
        return tableService.update(id, request);
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
