package ma.femme.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.femme.dto.AttendanceResponseRequest;
import ma.femme.dto.CoupleMessageRequest;
import ma.femme.dto.CoupleMessageResponseDTO;
import ma.femme.dto.InvitationResponseDTO;
import ma.femme.dto.MenuResponseDTO;
import ma.femme.service.InvitationService;
import ma.femme.service.MenuService;

@RestController
@RequestMapping("/rsvp")
@RequiredArgsConstructor
public class RsvpController {

  private final InvitationService invitationService;
  private final MenuService menuService;

  // OBSOLETO: el invitado ya no elige su menú (lo asigna el sistema), así
  // que el frontend actual no usa este endpoint. Se conserva temporalmente
  // para que una versión anterior del frontend, que aún lo llame durante el
  // despliegue, no deje de cargar la invitación. Eliminar en un próximo
  // release junto con MenuService.listActive().
  @Deprecated
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
        invitationService.respondToAttendance(token, request));
  }

  // Mensaje opcional para los novios. PUT porque es idempotente: guardar
  // dos veces lo mismo deja un único mensaje (el de la invitación del
  // token). Texto vacío lo borra. Va aparte de la asistencia a propósito.
  @PutMapping("/{token}/message")
  public ResponseEntity<CoupleMessageResponseDTO> saveMessage(
      @PathVariable UUID token,
      @Valid @RequestBody CoupleMessageRequest request) {

    return ResponseEntity.ok(invitationService.saveCoupleMessage(token, request));
  }
}
