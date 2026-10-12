package ma.femme.service;

import lombok.RequiredArgsConstructor;
import ma.femme.dto.FloorPlanElementDTO;
import ma.femme.dto.SaveFloorPlanRequest;
import ma.femme.entity.FloorPlanElement;
import ma.femme.entity.TableWedding;
import ma.femme.repository.FloorPlanElementRepository;
import ma.femme.repository.TableWeddingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FloorPlanService {

  private final FloorPlanElementRepository elementRepository;
  private final TableWeddingRepository tableRepository;

  @Transactional(readOnly = true)
  public List<FloorPlanElementDTO> getPlan() {
    return elementRepository.findAllByOrderByIdAsc().stream()
      .map(this::toDto)
      .toList();
  }

  // Reemplaza el plano completo por el recibido: las piezas con id se
  // actualizan, las que no traen id se crean y las que ya no vienen se
  // eliminan. Devuelve las piezas en el MISMO orden en que llegaron, para
  // que el cliente pueda ponerle su id a las nuevas.
  @Transactional
  public List<FloorPlanElementDTO> savePlan(SaveFloorPlanRequest request) {
    Map<Long, FloorPlanElement> existing = new HashMap<>();
    for (FloorPlanElement element : elementRepository.findAll()) {
      existing.put(element.getId(), element);
    }

    Set<Long> keptIds = new HashSet<>();
    Set<Long> usedTableIds = new HashSet<>();
    List<FloorPlanElement> result = new ArrayList<>();

    for (FloorPlanElementDTO dto : request.elements()) {
      FloorPlanElement element;

      if (dto.id() != null) {
        element = existing.get(dto.id());
        if (element == null) {
          throw new IllegalArgumentException("No existe la pieza " + dto.id() + " en el plano");
        }
        keptIds.add(dto.id());
      } else {
        element = new FloorPlanElement();
      }

      apply(element, dto, usedTableIds);
      result.add(element);
    }

    // Se borran primero las que ya no están, para liberar su mesa antes de
    // volver a enlazarla (table_id es único).
    List<FloorPlanElement> removed = existing.values().stream()
      .filter(element -> !keptIds.contains(element.getId()))
      .toList();
    elementRepository.deleteAll(removed);
    elementRepository.flush();

    return elementRepository.saveAll(result).stream()
      .map(this::toDto)
      .toList();
  }

  private void apply(FloorPlanElement element, FloorPlanElementDTO dto, Set<Long> usedTableIds) {
    requireRange("x", dto.x(), -5000, 5000);
    requireRange("y", dto.y(), -5000, 5000);
    requireRange("width", dto.width(), 10, 3000);
    requireRange("height", dto.height(), 10, 3000);
    requireRange("rotation", dto.rotation(), -3600, 3600);

    element.setKind(dto.kind());
    element.setX(dto.x());
    element.setY(dto.y());
    element.setWidth(dto.width());
    element.setHeight(dto.height());
    element.setRotation(dto.rotation());
    element.setLabel(dto.label() == null || dto.label().isBlank() ? null : dto.label().trim());

    if (dto.kind().isTable()) {
      if (dto.tableId() == null) {
        throw new IllegalArgumentException("Una mesa del plano debe estar enlazada a una mesa");
      }
      if (!usedTableIds.add(dto.tableId())) {
        throw new IllegalArgumentException("La mesa " + dto.tableId() + " aparece dos veces en el plano");
      }
      TableWedding table = tableRepository.findById(dto.tableId())
        .orElseThrow(() -> new IllegalArgumentException("No existe la mesa " + dto.tableId()));
      element.setTableWedding(table);
    } else {
      element.setTableWedding(null);
    }
  }

  private void requireRange(String field, double value, double min, double max) {
    if (Double.isNaN(value) || value < min || value > max) {
      throw new IllegalArgumentException(
        "El valor de '" + field + "' debe estar entre " + (int) min + " y " + (int) max);
    }
  }

  private FloorPlanElementDTO toDto(FloorPlanElement element) {
    return FloorPlanElementDTO.builder()
      .id(element.getId())
      .kind(element.getKind())
      .x(element.getX())
      .y(element.getY())
      .width(element.getWidth())
      .height(element.getHeight())
      .rotation(element.getRotation())
      .label(element.getLabel())
      .tableId(element.getTableWedding() == null ? null : element.getTableWedding().getId())
      .build();
  }
}
