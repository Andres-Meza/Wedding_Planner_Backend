package ma.femme.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.femme.dto.AdminInvitationResponseDTO;
import ma.femme.dto.CreateGuestRequest;
import ma.femme.dto.CreateInvitationRequest;
import ma.femme.service.AdminInvitationService;

// Protegido por SecurityConfig: /admin/** exige el rol ADMIN.
@RestController
@RequestMapping("/admin/invitations")
@RequiredArgsConstructor
public class AdminInvitationController {

  private final AdminInvitationService adminInvitationService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public AdminInvitationResponseDTO create(
      @Valid @RequestBody CreateInvitationRequest request) {
    return adminInvitationService.createInvitation(request);
  }

  @GetMapping
  public List<AdminInvitationResponseDTO> list() {
    return adminInvitationService.listAll();
  }

  @GetMapping("/{id}")
  public AdminInvitationResponseDTO getById(@PathVariable Long id) {
    return adminInvitationService.getById(id);
  }

  @PostMapping("/{id}/guests")
  @ResponseStatus(HttpStatus.CREATED)
  public AdminInvitationResponseDTO addGuest(
      @PathVariable Long id,
      @Valid @RequestBody CreateGuestRequest request) {
    return adminInvitationService.addGuest(id, request);
  }
}
