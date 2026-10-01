package ma.femme.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.femme.dto.AttendanceResponseRequest;
import ma.femme.dto.InvitationResponseDTO;
import ma.femme.service.InvitationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/rsvp")
@RequiredArgsConstructor
public class RsvpController {

    private final InvitationService invitationService;

    // The token acts as an access credential: anyone with the link can view
    // and respond to the invitation without needing to log in.
    @GetMapping("/{token}")
    public ResponseEntity<InvitationResponseDTO> getInvitation(
            @PathVariable UUID token) {

        return ResponseEntity.ok(invitationService.getByToken(token));
    }

    @PostMapping("/{token}")
    public ResponseEntity<InvitationResponseDTO> confirmAttendance(
            @PathVariable UUID token,
            @Valid @RequestBody AttendanceResponseRequest request) {

        return ResponseEntity.ok(
                invitationService.respondToAttendance(token, request)
        );
    }
}