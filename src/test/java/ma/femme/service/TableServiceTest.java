package ma.femme.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ma.femme.dto.CreateTableRequest;
import ma.femme.dto.TableResponseDTO;
import ma.femme.entity.TableWedding;
import ma.femme.repository.TableAssignmentRepository;
import ma.femme.repository.TableWeddingRepository;

class TableServiceTest {

  private TableWeddingRepository tableRepository;
  private TableAssignmentRepository assignmentRepository;
  private TableService service;

  @BeforeEach
  void setUp() {
    tableRepository = mock(TableWeddingRepository.class);
    assignmentRepository = mock(TableAssignmentRepository.class);
    service = new TableService(tableRepository, assignmentRepository);
  }

  @Test
  void listAllReadsTheOccupancyOfEveryTableInOneQuery() {
    when(tableRepository.findAll()).thenReturn(List.of(table(1L, 8), table(2L, 8)));
    when(assignmentRepository.occupiedSpotsByTable()).thenReturn(Map.of(1L, 3L));

    List<TableResponseDTO> result = service.listAll();

    assertThat(result).extracting(TableResponseDTO::occupiedSpots).containsExactly(3L, 0L);
    verify(assignmentRepository, never()).countByTableWeddingId(anyLong());
  }

  @Test
  void capacityCannotDropBelowTheSeatedGuests() {
    when(tableRepository.findById(1L)).thenReturn(Optional.of(table(1L, 8)));
    when(assignmentRepository.countByTableWeddingId(1L)).thenReturn(3L);

    assertThatThrownBy(() -> service.update(1L, new CreateTableRequest("A", 2, null)))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void updateKeepsReportingTheCurrentOccupancy() {
    when(tableRepository.findById(1L)).thenReturn(Optional.of(table(1L, 8)));
    when(assignmentRepository.countByTableWeddingId(1L)).thenReturn(3L);

    TableResponseDTO result = service.update(1L, new CreateTableRequest("Principal", 6, "Jardín"));

    assertThat(result.nameOrNumber()).isEqualTo("Principal");
    assertThat(result.capacity()).isEqualTo(6);
    assertThat(result.occupiedSpots()).isEqualTo(3L);
  }

  private static TableWedding table(Long id, int capacity) {
    return TableWedding.builder().id(id).nameOrNumber("Mesa " + id).capacity(capacity).build();
  }
}
