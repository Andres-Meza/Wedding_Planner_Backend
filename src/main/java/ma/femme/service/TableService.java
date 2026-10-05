package ma.femme.service;

import lombok.RequiredArgsConstructor;
import ma.femme.dto.CreateTableRequest;
import ma.femme.dto.TableResponseDTO;
import ma.femme.entity.TableWedding;
import ma.femme.repository.TableAssignmentRepository;
import ma.femme.repository.TableWeddingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
        long occupiedSpots =
                tableAssignmentRepository.countByTableWeddingId(table.getId());

        return TableResponseDTO.builder()
                .id(table.getId())
                .nameOrNumber(table.getNameOrNumber())
                .capacity(table.getCapacity())
                .zone(table.getArea())
                .occupiedSpots(occupiedSpots)
                .build();
    }
}
