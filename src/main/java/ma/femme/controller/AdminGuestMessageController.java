package ma.femme.controller;

import lombok.RequiredArgsConstructor;
import ma.femme.dto.GuestMessageFilter;
import ma.femme.dto.GuestMessagePageDTO;
import ma.femme.entity.InvitationType;
import ma.femme.service.GuestMessageService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// Protegido por SecurityConfig: /admin/** exige el rol ADMIN.
@RestController
@RequestMapping("/admin/guest-messages")
@RequiredArgsConstructor
public class AdminGuestMessageController {

  private final GuestMessageService guestMessageService;

  // page empieza en 0. size se limita a 50. filter: WITH_MESSAGE (por
  // defecto) o WITHOUT_MESSAGE. type: INDIVIDUAL o FAMILY (opcional).
  // search busca en nombre de la persona/familia, de sus invitados y en el
  // contenido del mensaje.
  @GetMapping
  public GuestMessagePageDTO list(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) String search,
      @RequestParam(required = false) InvitationType type,
      @RequestParam(required = false) GuestMessageFilter filter) {

    return guestMessageService.list(page, size, search, type, filter);
  }
}
