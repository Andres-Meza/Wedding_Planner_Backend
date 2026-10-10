package ma.femme.service;

import lombok.RequiredArgsConstructor;
import ma.femme.dto.MenuResponseDTO;
import ma.femme.dto.SaveMenuRequest;
import ma.femme.entity.Menu;
import ma.femme.exception.MenuInUseException;
import ma.femme.repository.GuestRepository;
import ma.femme.repository.MenuRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

  // Solo los menús que se pueden ofrecer hoy — para el formulario de RSVP.
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

    Menu menu = Menu.builder()
      .name(name)
      .starter(request.starter().trim())
      .mainCourse(request.mainCourse().trim())
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

    menu.setName(name);
    menu.setStarter(request.starter().trim());
    menu.setMainCourse(request.mainCourse().trim());
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

  private Menu findOrThrow(Long id) {
    return menuRepository.findById(id)
      .orElseThrow(() -> new IllegalArgumentException("No existe un menú con id " + id));
  }
}
