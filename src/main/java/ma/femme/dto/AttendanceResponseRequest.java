package ma.femme.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record AttendanceResponseRequest(

        // Name of the person filling out the form. It can be the guest
        // confirming only their own attendance, or the person responsible
        // for the invitation confirming several/all guests at once.
        @NotBlank String respondedBy,

        @NotEmpty @Valid List<GuestAttendanceResponseDTO> responses
) {
}