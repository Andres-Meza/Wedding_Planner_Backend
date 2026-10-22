package ma.femme.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ma.femme.dto.MenuResponseDTO;
import ma.femme.dto.SaveMainCourseRequest;
import ma.femme.dto.SaveMenuRequest;
import ma.femme.entity.MainCourse;
import ma.femme.entity.Menu;
import ma.femme.exception.MenuInUseException;
import ma.femme.repository.GuestRepository;
import ma.femme.repository.MenuRepository;

@Service
@RequiredArgsConstructor
public class MenuService {

  private final MenuRepository menuRepository;
  private final GuestRepository guestRepository;

  // Todos los menús (activos e inactivos) — para el panel de admin.
  @Transactional(readOnly = true)
  public List<MenuResponseDTO> listAll() {
    return menuRepository.findAllByOrderByKidsAscIdAsc().stream()
        .map(MenuResponseDTO::fromEntity)
        .toList();
  }

  // Solo los menús activos. Lo usa únicamente el endpoint obsoleto
  // GET /rsvp/menu-options.
  @Transactional(readOnly = true)
  public List<MenuResponseDTO> listActive() {
    return menuRepository.findByActiveTrueOrderByKidsAscIdAsc().stream()
        .map(MenuResponseDTO::fromEntity)
        .toList();
  }

  @Transactional
  public MenuResponseDTO create(SaveMenuRequest request) {
    String name = request.name().trim();

    if (menuRepository.existsByNameIgnoreCase(name)) {
      throw new IllegalArgumentException("Ya existe un menú llamado '" + name + "'");
    }

    MainCourse mainCourse = toMainCourse(request.mainCourse());
    validateMainCourse(mainCourse, request.isKids());

    Menu menu = Menu.builder()
        .name(name)
        .starter(request.starter().trim())
        .mainCourse(mainCourse)
        .dessert(request.dessert().trim())
        .drink(request.drink().trim())
        .kids(request.isKids())
        .build();

    return MenuResponseDTO.fromEntity(menuRepository.save(menu));
  }

  @Transactional
  public MenuResponseDTO update(Long id, SaveMenuRequest request) {
    Menu menu = findOrThrow(id);
    String name = request.name().trim();

    if (menuRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
      throw new IllegalArgumentException("Ya existe un menú llamado '" + name + "'");
    }

    // Cambiar adulto <-> infantil dejaría invitados con un menú que no les
    // corresponde, así que solo se permite mientras nadie lo haya elegido.
    if (menu.isKids() != request.isKids() && guestRepository.existsByMenuId(id)) {
      throw new IllegalArgumentException(
          "No puedes cambiar el tipo (adulto/infantil) de un menú que ya tiene invitados asociados");
    }

    MainCourse mainCourse = toMainCourse(request.mainCourse());
    validateMainCourse(mainCourse, request.isKids());

    menu.setName(name);
    menu.setStarter(request.starter().trim());
    menu.setMainCourse(mainCourse);
    menu.setDessert(request.dessert().trim());
    menu.setDrink(request.drink().trim());
    menu.setKids(request.isKids());

    return MenuResponseDTO.fromEntity(menu);
  }

  @Transactional
  public MenuResponseDTO setActive(Long id, boolean active) {
    Menu menu = findOrThrow(id);
    menu.setActive(active);
    return MenuResponseDTO.fromEntity(menu);
  }

  @Transactional
  public void delete(Long id) {
    Menu menu = findOrThrow(id);

    if (guestRepository.existsByMenuId(id)) {
      throw new MenuInUseException(menu.getName());
    }

    menuRepository.delete(menu);
  }

  private MainCourse toMainCourse(SaveMainCourseRequest request) {
    return MainCourse.builder()
        .protein1(request.protein1().trim())
        .protein2(blankToNull(request.protein2()))
        .accompaniment(blankToNull(request.accompaniment()))
        .salad(blankToNull(request.salad()))
        .build();
  }

  // Un menú de adultos define el plato fuerte completo (dos proteínas,
  // acompañamiento y ensalada). En los infantiles solo la proteína 1 es
  // obligatoria: pueden ser un plato único (pizza, pasta, nuggets...) y hoy
  // no hay una regla de negocio que les exija los cuatro componentes.
  private void validateMainCourse(MainCourse mainCourse, boolean isKids) {
    if (isKids) {
      return;
    }

    List<String> missing = new ArrayList<>();
    if (mainCourse.getProtein2() == null) {
      missing.add("proteína 2");
    }
    if (mainCourse.getAccompaniment() == null) {
      missing.add("acompañamiento");
    }
    if (mainCourse.getSalad() == null) {
      missing.add("ensalada");
    }

    if (!missing.isEmpty()) {
      throw new IllegalArgumentException(
          "El plato fuerte de un menú para adultos debe incluir proteína 1, proteína 2, "
              + "acompañamiento y ensalada. Falta: " + String.join(", ", missing));
    }
  }

  private String blankToNull(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return value.trim();
  }

  private Menu findOrThrow(Long id) {
    return menuRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("No existe un menú con id " + id));
  }
}
