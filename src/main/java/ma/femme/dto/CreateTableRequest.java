package ma.femme.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateTableRequest(
        @NotBlank String nameOrNumber,
        @NotNull @Min(1) Integer capacity,
        String zone
) {
}