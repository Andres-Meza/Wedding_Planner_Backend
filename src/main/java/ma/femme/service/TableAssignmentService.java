package ma.femme.service;

import lombok.RequiredArgsConstructor;
import ma.femme.dto.AssignedGuestDTO;
import ma.femme.dto.AssignmentResultDTO;
import ma.femme.dto.ManualAssignmentRequest;
import ma.femme.dto.TableWithGuestsDTO;
import ma.femme.entity.AttendanceStatus;
import ma.femme.entity.Guest;
import ma.femme.entity.TableAssignment;
import ma.femme.entity.TableWedding;
import ma.femme.repository.GuestRepository;
import ma.femme.repository.TableAssignmentRepository;
import ma.femme.repository.TableWeddingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TableAssignmentService {

    private final TableWeddingRepository tableRepository;
    private final GuestRepository guestRepository;
    private final TableAssignmentRepository tableAssignmentRepository;

    /**
     * Assigns a table to all guests who confirmed their attendance and do not
     * have a table yet. Groups guests by invitation (attempts to seat each
     * family together at a single table); if a group does not fit completely
     * at any available table, it distributes the guests by filling tables
     * one available spot at a time.
     * Guests who already have a table are not reassigned — use the manual
     * endpoint for that.
     */
    @Transactional
    public AssignmentResultDTO assignAutomatically() {

        List<TableCapacity> tablesWithAvailableCapacity =
                loadTablesWithAvailableCapacity();

        List<Guest> confirmedGuestsWithoutTable = guestRepository
                .findByAttendance(AttendanceStatus.CONFIRMED).stream()
                .filter(guest ->
                        !tableAssignmentRepository.existsByGuestId(guest.getId()))
                .toList();

        // Group by invitation and sort the largest groups first:
        // this places larger families first, which are harder to fit,
        // leaving smaller spaces for the end.
        Map<Long, List<Guest>> groupsByInvitation =
                confirmedGuestsWithoutTable.stream()
                        .collect(Collectors.groupingBy(
                                guest -> guest.getInvitation().getId()));

        List<List<Guest>> sortedGroups = groupsByInvitation.values().stream()
                .sorted(Comparator.comparingInt(List<Guest>::size).reversed())
                .toList();

        List<TableAssignment> newAssignments = new ArrayList<>();
        List<String> unassignedGuests = new ArrayList<>();

        for (List<Guest> group : sortedGroups) {

            TableCapacity tableForCompleteGroup =
                    findTableWithCapacityFor(
                            tablesWithAvailableCapacity,
                            group.size());

            if (tableForCompleteGroup != null) {

                for (Guest guest : group) {
                    newAssignments.add(
                            createAssignment(
                                    guest,
                                    tableForCompleteGroup.table,
                                    false));

                    tableForCompleteGroup.remainingCapacity--;
                }

            } else {

                // The entire family does not fit together: distribute each guest
                // individually into the first available spot.
                for (Guest guest : group) {

                    TableCapacity availableCapacity =
                            findTableWithCapacityFor(
                                    tablesWithAvailableCapacity,
                                    1);

                    if (availableCapacity == null) {
                        unassignedGuests.add(guest.getFullName());
                        continue;
                    }

                    newAssignments.add(
                            createAssignment(
                                    guest,
                                    availableCapacity.table,
                                    false));

                    availableCapacity.remainingCapacity--;
                }
            }
        }

        tableAssignmentRepository.saveAll(newAssignments);

        return AssignmentResultDTO.builder()
                .totalAssigned(newAssignments.size())
                .totalUnassigned(unassignedGuests.size())
                .unassignedGuests(unassignedGuests)
                .build();
    }

    @Transactional
    public void assignManually(ManualAssignmentRequest request) {

        Guest guest = guestRepository.findById(request.guestId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No guest exists with id " + request.guestId()));

        TableWedding table = tableRepository.findById(request.tableId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No table exists with id " + request.tableId()));

        long occupiedSpots =
                tableAssignmentRepository.countByTableWeddingId(table.getId());

        if (occupiedSpots >= table.getCapacity()) {
            throw new IllegalArgumentException(
                    "Table '" + table.getNameOrNumber() + "' is already full");
        }

        // If the guest already had a table, remove that assignment before
        // creating the new one (prevents violating the UNIQUE constraint
        // on guest_id).
        tableAssignmentRepository.deleteByGuestId(guest.getId());

        tableAssignmentRepository.save(
                createAssignment(guest, table, true));
    }

    @Transactional
    public void removeAssignment(Long guestId) {
        tableAssignmentRepository.deleteByGuestId(guestId);
    }

    @Transactional(readOnly = true)
    public List<TableWithGuestsDTO> getTableLayout() {

        return tableRepository.findAll().stream()
                .map(table -> TableWithGuestsDTO.builder()
                        .tableId(table.getId())
                        .nameOrNumber(table.getNameOrNumber())
                        .capacity(table.getCapacity())
                        .guests(
                                tableAssignmentRepository.findByTableWeddingId(table.getId()).stream()
                                        .map(assignment -> AssignedGuestDTO.builder()
                                                .guestId(assignment.getGuest().getId())
                                                .fullName(assignment.getGuest().getFullName())
                                                .build())
                                        .toList())
                        .build())
                .toList();
    }

    private List<TableCapacity> loadTablesWithAvailableCapacity() {

        List<TableCapacity> result = new ArrayList<>();

        for (TableWedding table : tableRepository.findAll()) {

            long occupiedSpots =
                    tableAssignmentRepository.countByTableWeddingId(table.getId());

            int availableSpots =
                    table.getCapacity() - (int) occupiedSpots;

            if (availableSpots > 0) {
                result.add(new TableCapacity(table, availableSpots));
            }
        }

        return result;
    }

    private TableCapacity findTableWithCapacityFor(
            List<TableCapacity> tablesWithCapacity,
            int requiredSpots) {

        return tablesWithCapacity.stream()
                .filter(capacity ->
                        capacity.remainingCapacity >= requiredSpots)
                .min(Comparator.comparingInt(
                        capacity -> capacity.remainingCapacity))
                .orElse(null);
    }

    private TableAssignment createAssignment(
            Guest guest,
            TableWedding table,
            boolean manual) {

        return TableAssignment.builder()
                .guest(guest)
                .tableWedding(table)
                .manuallyAssigned(manual)
                .build();
    }

    /**
     * Mutable in-memory state for the remaining capacity of a table
     * during the assignment algorithm.
     */
    private static final class TableCapacity {

        private final TableWedding table;
        private int remainingCapacity;

        private TableCapacity(
                TableWedding table,
                int remainingCapacity) {
            this.table = table;
            this.remainingCapacity = remainingCapacity;
        }
    }
}
