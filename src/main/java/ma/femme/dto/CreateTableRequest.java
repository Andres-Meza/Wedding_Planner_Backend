package ma.femme.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateTableRequest(
    @NotBlank @Size(max = 50) String nameOrNumber,
    @NotNull @Min(1) Integer capacity,
    @Size(max = 100) String zone) {
}
