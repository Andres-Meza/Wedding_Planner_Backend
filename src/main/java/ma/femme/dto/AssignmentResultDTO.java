package ma.femme.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record AssignmentResultDTO(
        int totalAssigned,
        int totalUnassigned,
        List<String> unassignedGuests
) {
}