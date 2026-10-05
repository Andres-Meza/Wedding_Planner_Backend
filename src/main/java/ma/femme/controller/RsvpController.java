package ma.femme.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.femme.dto.AttendanceResponseRequest;
import ma.femme.dto.InvitationResponseDTO;
import ma.femme.dto.MenuCatalogDTO;
import ma.femme.service.InvitationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/rsvp")
@RequiredArgsConstructor
public class RsvpController {

    private final InvitationService invitationService;

    // Catálogo fijo de opciones de menú (adultos y niños) — público porque
    // el formulario de RSVP lo necesita antes de que el invitado confirme.
    @GetMapping("/menu-options")
    public ResponseEntity<MenuCatalogDTO> getMenuOptions() {
        return ResponseEntity.ok(invitationService.getMenuCatalog());
    }

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
