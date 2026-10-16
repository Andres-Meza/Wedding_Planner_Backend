package ma.femme.service;

import ma.femme.entity.Menu;
import ma.femme.exception.DefaultMenuUnavailableException;
import ma.femme.repository.MenuRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

// Decide qué menú recibe un invitado automáticamente:
//   adulto -> "Menú 1"          menor -> "Menú Infantil 1"
// Los nombres se pueden cambiar en application.yml (app.menus.*) si el
// admin renombra esos menús. Nunca cae en "otro menú": si el predeterminado
// no existe o está inactivo, lanza DefaultMenuUnavailableException (409).
@Service
public class DefaultMenuService {

  private final MenuRepository menuRepository;
  private final String adultMenuName;
  private final String kidsMenuName;

  public DefaultMenuService(
    MenuRepository menuRepository,
    @Value("${app.menus.default-adult-name:Menú 1}") String adultMenuName,
    @Value("${app.menus.default-kids-name:Menú Infantil 1}") String kidsMenuName) {
    this.menuRepository = menuRepository;
    this.adultMenuName = adultMenuName;
    this.kidsMenuName = kidsMenuName;
  }

  // Debe llamarse dentro de la transacción que guarda al invitado: si lanza,
  // esa transacción se revierte y no queda nada a medio guardar.
  public Menu defaultMenuFor(boolean minor) {
    String name = minor ? kidsMenuName : adultMenuName;
    String category = minor ? "infantil" : "de adultos";

    Menu menu = menuRepository.findByNameIgnoreCaseAndKids(name, minor)
      .orElseThrow(() -> new DefaultMenuUnavailableException(
        "No existe el menú " + category + " predeterminado '" + name
          + "'. Créalo en la sección Menús antes de registrar invitados."));

    if (!menu.isActive()) {
      throw new DefaultMenuUnavailableException(
        "El menú " + category + " predeterminado '" + name
          + "' está inactivo. Actívalo en la sección Menús antes de registrar invitados.");
    }

    return menu;
  }
}
