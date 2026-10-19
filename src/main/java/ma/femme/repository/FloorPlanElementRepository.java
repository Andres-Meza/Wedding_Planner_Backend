package ma.femme.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import ma.femme.entity.FloorPlanElement;

public interface FloorPlanElementRepository extends JpaRepository<FloorPlanElement, Long> {

  List<FloorPlanElement> findAllByOrderByIdAsc();
}
