package ma.femme.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ma.femme.entity.Menu;
import ma.femme.exception.DefaultMenuUnavailableException;
import ma.femme.repository.MenuRepository;

class DefaultMenuServiceTest {

  private MenuRepository menuRepository;
  private DefaultMenuService service;

  @BeforeEach
  void setUp() {
    menuRepository = mock(MenuRepository.class);
    service = new DefaultMenuService(menuRepository, "Menú 1", "Menú Infantil 1");
  }

  @Test
  void adultGetsMenu1() {
    Menu menu = menu(1L, "Menú 1", false, true);
    when(menuRepository.findByNameIgnoreCaseAndKids("Menú 1", false)).thenReturn(Optional.of(menu));

    assertThat(service.defaultMenuFor(false)).isSameAs(menu);
  }

  @Test
  void minorGetsKidsMenu1() {
    Menu menu = menu(2L, "Menú Infantil 1", true, true);
    when(menuRepository.findByNameIgnoreCaseAndKids("Menú Infantil 1", true)).thenReturn(Optional.of(menu));

    assertThat(service.defaultMenuFor(true)).isSameAs(menu);
  }

  @Test
  void missingMenuIsReportedInsteadOfPickingAnotherOne() {
    when(menuRepository.findByNameIgnoreCaseAndKids("Menú 1", false)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.defaultMenuFor(false))
        .isInstanceOf(DefaultMenuUnavailableException.class)
        .hasMessageContaining("Menú 1")
        .hasMessageContaining("No existe");
  }

  @Test
  void inactiveMenuIsReported() {
    when(menuRepository.findByNameIgnoreCaseAndKids("Menú Infantil 1", true))
        .thenReturn(Optional.of(menu(2L, "Menú Infantil 1", true, false)));

    assertThatThrownBy(() -> service.defaultMenuFor(true))
        .isInstanceOf(DefaultMenuUnavailableException.class)
        .hasMessageContaining("inactivo");
  }

  static Menu menu(Long id, String name, boolean kids, boolean active) {
    return Menu.builder().id(id).name(name).kids(kids).active(active).build();
  }
}
