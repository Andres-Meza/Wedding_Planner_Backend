package ma.femme.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record TableWithGuestsDTO(
    Long tableId,
    String nameOrNumber,
    Integer capacity,
    List<AssignedGuestDTO> guests) {
}
