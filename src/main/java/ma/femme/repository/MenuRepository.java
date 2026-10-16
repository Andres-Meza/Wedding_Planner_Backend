package ma.femme.repository;

import ma.femme.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MenuRepository extends JpaRepository<Menu, Long> {

  // Adultos primero (kids = false), luego infantiles; dentro de cada grupo
  // por orden de creación.
  List<Menu> findAllByOrderByKidsAscIdAsc();

  List<Menu> findByActiveTrueOrderByKidsAscIdAsc();

  // El nombre es único sin importar mayúsculas (índice uq_menu_name), así que
  // como máximo hay una coincidencia.
  Optional<Menu> findByNameIgnoreCaseAndKids(String name, boolean kids);

  boolean existsByNameIgnoreCase(String name);

  boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
