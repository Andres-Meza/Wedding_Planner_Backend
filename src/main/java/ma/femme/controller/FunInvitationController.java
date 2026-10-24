package ma.femme.controller;

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
import ma.femme.dto.FunAnswerRequest;
import ma.femme.dto.FunInvitationDTO;
import ma.femme.service.FunInvitationService;

// Invitación divertida (pública). El token individual del invitado es su
// credencial, igual que en /rsvp: quien tiene el enlace ve y responde SOLO
// el cuestionario de ese invitado. Ningún endpoint recibe un id de invitado.
@RestController
@RequestMapping("/fun")
@RequiredArgsConstructor
public class FunInvitationController {

  private final FunInvitationService funInvitationService;

  // Estado actual: preguntas, respuestas ya guardadas y, si ya terminó, su
  // veredicto. Con esto el frontend retoma donde se quedó el invitado.
  @GetMapping("/{token}")
  public ResponseEntity<FunInvitationDTO> get(@PathVariable UUID token) {
    return ResponseEntity.ok(funInvitationService.getByToken(token));
  }

  // PUT porque es idempotente: guardar dos veces la misma respuesta deja una
  // sola respuesta por pregunta (reintentar tras un error de red es seguro).
  @PutMapping("/{token}/answers/{questionKey}")
  public ResponseEntity<FunInvitationDTO> saveAnswer(
      @PathVariable UUID token,
      @PathVariable String questionKey,
      @Valid @RequestBody FunAnswerRequest request) {

    return ResponseEntity.ok(
        funInvitationService.saveAnswer(token, questionKey, request.optionKey()));
  }

  // Cierra el cuestionario y guarda el veredicto. Si ya estaba cerrado
  // devuelve el mismo resultado, así que reintentar es seguro.
  @PostMapping("/{token}/complete")
  public ResponseEntity<FunInvitationDTO> complete(@PathVariable UUID token) {
    return ResponseEntity.ok(funInvitationService.complete(token));
  }
}
