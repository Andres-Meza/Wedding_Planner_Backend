package ma.femme.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.femme.dto.MenuResponseDTO;
import ma.femme.dto.SaveMenuRequest;
import ma.femme.dto.UpdateMenuStatusRequest;
import ma.femme.service.MenuService;

@RestController
@RequestMapping("/admin/menus")
@RequiredArgsConstructor
public class AdminMenuController {

  private final MenuService menuService;

  @GetMapping
  public List<MenuResponseDTO> list() {
    return menuService.listAll();
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public MenuResponseDTO create(@Valid @RequestBody SaveMenuRequest request) {
    return menuService.create(request);
  }

  @PutMapping("/{id}")
  public MenuResponseDTO update(
      @PathVariable Long id,
      @Valid @RequestBody SaveMenuRequest request) {
    return menuService.update(id, request);
  }

  @PatchMapping("/{id}/active")
  public MenuResponseDTO setActive(
      @PathVariable Long id,
      @Valid @RequestBody UpdateMenuStatusRequest request) {
    return menuService.setActive(id, request.active());
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    menuService.delete(id);
  }
}
