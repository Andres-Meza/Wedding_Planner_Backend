package ma.femme.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record MenuCatalogDTO(
    List<MenuOptionDTO> adultOptions,
    List<MenuOptionDTO> kidsOptions) {
}
