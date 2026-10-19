package ma.femme.dto;

import lombok.Builder;

@Builder
public record TableResponseDTO(
    Long id,
    String nameOrNumber,
    Integer capacity,
    String zone,
    long occupiedSpots) {
}
