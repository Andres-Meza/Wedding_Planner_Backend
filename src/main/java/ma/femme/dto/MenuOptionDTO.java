package ma.femme.dto;

import lombok.Builder;

@Builder
public record MenuOptionDTO(
        String value,
        String label
) {
}
