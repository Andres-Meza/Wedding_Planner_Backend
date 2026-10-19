package ma.femme.dto;

import java.util.List;

import lombok.Builder;

@Builder
public record AssignmentResultDTO(
    int totalAssigned,
    int totalUnassigned,
    List<String> unassignedGuests) {
}
