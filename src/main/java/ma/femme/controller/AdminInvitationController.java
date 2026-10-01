package ma.femme.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.femme.dto.AdminInvitationResponseDTO;
import ma.femme.dto.CreateGuestRequest;
import ma.femme.dto.CreateInvitationRequest;
import ma.femme.service.AdminInvitationService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// TODO: Protect this controller with authentication (Spring Security)
// before deploying to production — it remains open for development for now.
@RestController
@RequestMapping("/api/admin/invitations")
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