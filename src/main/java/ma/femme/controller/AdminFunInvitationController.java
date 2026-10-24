package ma.femme.controller;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import ma.femme.dto.AdminFunGuestDTO;
import ma.femme.dto.AdminFunGuestDetailDTO;
import ma.femme.dto.AdminFunPageDTO;
import ma.femme.dto.AdminFunStatsDTO;
import ma.femme.dto.FunQuizState;
import ma.femme.service.AdminFunInvitationService;

// Protegido por SecurityConfig: /admin/** exige el rol ADMIN.
@RestController
@RequestMapping("/admin/fun-invitations")
@RequiredArgsConstructor
public class AdminFunInvitationController {

  private static final MediaType CSV = MediaType.parseMediaType("text/csv; charset=UTF-8");

  private final AdminFunInvitationService adminFunInvitationService;

  // page empieza en 0 y size se limita a 50. search busca por nombre del
  // invitado o de su persona/familia; invitationId filtra una familia;
  // state: NOT_STARTED, IN_PROGRESS o COMPLETED. Todos son opcionales.
  @GetMapping
  public AdminFunPageDTO list(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) String search,
      @RequestParam(required = false) Long invitationId,
      @RequestParam(required = false) FunQuizState state) {

    return adminFunInvitationService.list(page, size, search, invitationId, state);
  }

  @GetMapping("/stats")
  public AdminFunStatsDTO stats() {
    return adminFunInvitationService.getStats();
  }

  @GetMapping("/export")
  public ResponseEntity<byte[]> export() {
    return ResponseEntity.ok()
        .contentType(CSV)
        .header(HttpHeaders.CONTENT_DISPOSITION,
            ContentDisposition.attachment().filename("invitacion-divertida.csv").build().toString())
        .body(adminFunInvitationService.exportCsv());
  }

  @GetMapping("/{guestId}")
  public AdminFunGuestDetailDTO detail(@PathVariable Long guestId) {
    return adminFunInvitationService.getDetail(guestId);
  }

  // Regenerar invalida el enlace divertido anterior de ese invitado, así que
  // exige confirm=true de forma explícita; sin él no cambia nada.
  @PostMapping("/{guestId}/regenerate-token")
  public AdminFunGuestDTO regenerateToken(
      @PathVariable Long guestId,
      @RequestParam(defaultValue = "false") boolean confirm) {

    if (!confirm) {
      throw new IllegalArgumentException(
          "Regenerar el enlace invalida el anterior; envía confirm=true para confirmarlo");
    }
    return adminFunInvitationService.regenerateToken(guestId);
  }
}
