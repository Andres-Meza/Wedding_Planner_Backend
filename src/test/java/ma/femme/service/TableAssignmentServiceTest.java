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
import org.mockito.ArgumentCaptor;

import ma.femme.dto.AssignmentResultDTO;
import ma.femme.dto.ManualAssignmentRequest;
import ma.femme.dto.TableWithGuestsDTO;
import ma.femme.entity.AttendanceStatus;
import ma.femme.entity.Guest;
import ma.femme.entity.Invitation;
import ma.femme.entity.TableAssignment;
import ma.femme.entity.TableWedding;
import ma.femme.repository.GuestRepository;
import ma.femme.repository.TableAssignmentRepository;
import ma.femme.repository.TableWeddingRepository;

class TableAssignmentServiceTest {

  private TableWeddingRepository tableRepository;
  private GuestRepository guestRepository;
  private TableAssignmentRepository assignmentRepository;
  private TableAssignmentService service;

  private final Invitation family = Invitation.builder().id(10L).build();

  @BeforeEach
  void setUp() {
    tableRepository = mock(TableWeddingRepository.class);
    guestRepository = mock(GuestRepository.class);
    assignmentRepository = mock(TableAssignmentRepository.class);
    service = new TableAssignmentService(tableRepository, guestRepository, assignmentRepository);
  }

  @Test
  void aFamilyIsSeatedTogetherAtTheTableWithTheLeastRoomThatFitsIt() {
    TableWedding big = table(1L, "A", 10);
    TableWedding snug = table(2L, "B", 4);
    when(tableRepository.findAll()).thenReturn(List.of(big, snug));
    when(guestRepository.findWithoutTableByAttendance(AttendanceStatus.CONFIRMED))
        .thenReturn(List.of(guest(1L, "Ana"), guest(2L, "Luis"), guest(3L, "Leo")));

    AssignmentResultDTO result = service.assignAutomatically();

    assertThat(result.totalAssigned()).isEqualTo(3);
    assertThat(result.totalUnassigned()).isZero();
    assertThat(savedAssignments())
        .extracting(assignment -> assignment.getTableWedding().getId())
        .containsOnly(2L);
  }

  @Test
  void occupiedSpotsReduceTheRoomAndTheFamilyIsSplitWhenItDoesNotFit() {
    TableWedding table = table(1L, "A", 2);
    when(tableRepository.findAll()).thenReturn(List.of(table));
    when(assignmentRepository.occupiedSpotsByTable()).thenReturn(Map.of(1L, 1L));
    when(guestRepository.findWithoutTableByAttendance(AttendanceStatus.CONFIRMED))
        .thenReturn(List.of(guest(1L, "Ana"), guest(2L, "Luis")));

    AssignmentResultDTO result = service.assignAutomatically();

    assertThat(result.totalAssigned()).isEqualTo(1);
    assertThat(result.totalUnassigned()).isEqualTo(1);
    assertThat(result.unassignedGuests()).containsExactly("Luis");
    verify(assignmentRepository, never()).countByTableWeddingId(anyLong());
  }

  @Test
  void manualAssignmentToAFullTableIsRejected() {
    TableWedding full = table(1L, "A", 1);
    Guest guest = guest(1L, "Ana");
    when(guestRepository.findById(1L)).thenReturn(Optional.of(guest));
    when(tableRepository.findById(1L)).thenReturn(Optional.of(full));
    when(assignmentRepository.findByGuestId(1L)).thenReturn(Optional.empty());
    when(assignmentRepository.countByTableWeddingId(1L)).thenReturn(1L);

    assertThatThrownBy(() -> service.assignManually(new ManualAssignmentRequest(1L, 1L, null)))
        .isInstanceOf(IllegalArgumentException.class);
    verify(assignmentRepository, never()).save(org.mockito.ArgumentMatchers.any());
  }

  @Test
  void layoutGroupsGuestsByTableWithoutQueryingEachTable() {
    TableWedding first = table(1L, "A", 4);
    TableWedding second = table(2L, "B", 4);
    TableWedding empty = table(3L, "C", 4);
    when(tableRepository.findAll()).thenReturn(List.of(first, second, empty));
    when(assignmentRepository.findAllWithGuestAndTable()).thenReturn(List.of(
        assignment(guest(1L, "Ana"), first, 0),
        assignment(guest(2L, "Luis"), first, null),
        assignment(guest(3L, "Leo"), second, 2)));

    List<TableWithGuestsDTO> layout = service.getTableLayout();

    assertThat(layout).extracting(table -> table.guests().size()).containsExactly(2, 1, 0);
    assertThat(layout.get(1).guests().get(0).seatIndex()).isEqualTo(2);
    verify(assignmentRepository, never()).countByTableWeddingId(anyLong());
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  private List<TableAssignment> savedAssignments() {
    ArgumentCaptor<List<TableAssignment>> captor = ArgumentCaptor.forClass((Class) List.class);
    verify(assignmentRepository).saveAll(captor.capture());
    return captor.getValue();
  }

  private static TableWedding table(Long id, String name, int capacity) {
    return TableWedding.builder().id(id).nameOrNumber(name).capacity(capacity).build();
  }

  private Guest guest(Long id, String name) {
    return Guest.builder().id(id).fullName(name).invitation(family).build();
  }

  private static TableAssignment assignment(Guest guest, TableWedding table, Integer seat) {
    return TableAssignment.builder().guest(guest).tableWedding(table).seatIndex(seat).build();
  }
}
