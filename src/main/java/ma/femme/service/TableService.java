package ma.femme.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ma.femme.dto.CreateTableRequest;
import ma.femme.dto.TableResponseDTO;
import ma.femme.entity.TableWedding;
import ma.femme.repository.TableAssignmentRepository;
import ma.femme.repository.TableWeddingRepository;

@Service
@RequiredArgsConstructor
public class TableService {

  private final TableWeddingRepository tableRepository;
  private final TableAssignmentRepository tableAssignmentRepository;

  @Transactional
  public TableResponseDTO create(CreateTableRequest request) {
    TableWedding table = TableWedding.builder()
        .nameOrNumber(request.nameOrNumber())
        .capacity(request.capacity())
        .area(request.zone())
        .build();

    TableWedding saved = tableRepository.save(table);

    return mapToDTO(saved);
  }

  // Renombrar la mesa o cambiar su capacidad (el plano lo usa para ajustar
  // cuántas sillas dibuja).
  @Transactional
  public TableResponseDTO update(Long id, CreateTableRequest request) {
    TableWedding table = tableRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException(
            "No table exists with id " + id));

    long occupiedSpots = tableAssignmentRepository.countByTableWeddingId(id);

    if (request.capacity() < occupiedSpots) {
      throw new IllegalArgumentException(
          "No puedes reducir la capacidad a " + request.capacity()
              + ": hay " + occupiedSpots + " invitados sentados en esta mesa");
    }

    if (tableAssignmentRepository.existsByTableWeddingIdAndSeatIndexGreaterThanEqual(
        id, request.capacity())) {
      throw new IllegalArgumentException(
          "Hay invitados sentados en sillas que desaparecerían; muévelos antes de reducir la capacidad");
    }

    table.setNameOrNumber(request.nameOrNumber());
    table.setCapacity(request.capacity());
    table.setArea(request.zone());

    return mapToDTO(table);
  }

  @Transactional(readOnly = true)
  public List<TableResponseDTO> listAll() {
    return tableRepository.findAll().stream()
        .map(this::mapToDTO)
        .toList();
  }

  @Transactional
  public void delete(Long id) {
    if (!tableRepository.existsById(id)) {
      throw new IllegalArgumentException(
          "No table exists with id " + id);
    }

    tableRepository.deleteById(id);
  }

  private TableResponseDTO mapToDTO(TableWedding table) {
    long occupiedSpots = tableAssignmentRepository.countByTableWeddingId(table.getId());

    return TableResponseDTO.builder()
        .id(table.getId())
        .nameOrNumber(table.getNameOrNumber())
        .capacity(table.getCapacity())
        .zone(table.getArea())
        .occupiedSpots(occupiedSpots)
        .build();
  }
}
