package ma.femme.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.femme.dto.AttendanceResponseRequest;
import ma.femme.dto.InvitationResponseDTO;
import ma.femme.dto.MenuResponseDTO;
import ma.femme.service.InvitationService;
import ma.femme.service.MenuService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/rsvp")
@RequiredArgsConstructor
public class RsvpController {

  private final InvitationService invitationService;
  private final MenuService menuService;

  // Menús activos (adultos e infantiles) — público porque el formulario de
  // RSVP los necesita para armar los radio buttons. El frontend filtra por
  // isKids según la categoría de cada invitado.
  @GetMapping("/menu-options")
  public ResponseEntity<List<MenuResponseDTO>> getMenuOptions() {
    return ResponseEntity.ok(menuService.listActive());
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
