package ma.femme.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record AttendanceResponseRequest(

    // Name of the person filling out the form. It can be the guest
    // confirming only their own attendance, or the person responsible
    // for the invitation confirming several/all guests at once.
    @NotBlank String respondedBy,

    @NotEmpty @Valid List<GuestAttendanceResponseDTO> responses) {
}
