package ma.femme.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public record AttendanceResponseRequest(

    @NotEmpty @Valid List<GuestAttendanceResponseDTO> responses) {
}