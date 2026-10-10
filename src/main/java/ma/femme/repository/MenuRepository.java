package ma.femme.repository;

import ma.femme.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Long> {

  // Adultos primero (kids = false), luego infantiles; dentro de cada grupo
  // por orden de creación.
  List<Menu> findAllByOrderByKidsAscIdAsc();

  List<Menu> findByActiveTrueOrderByKidsAscIdAsc();

  boolean existsByNameIgnoreCase(String name);

  boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
