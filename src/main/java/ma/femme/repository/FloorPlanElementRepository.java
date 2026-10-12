package ma.femme.repository;

import ma.femme.entity.FloorPlanElement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FloorPlanElementRepository extends JpaRepository<FloorPlanElement, Long> {

  List<FloorPlanElement> findAllByOrderByIdAsc();
}
