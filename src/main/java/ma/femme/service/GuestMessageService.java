package ma.femme.service;

import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.RequiredArgsConstructor;
import ma.femme.dto.GuestMessageDTO;
import ma.femme.dto.GuestMessageFilter;
import ma.femme.dto.GuestMessagePageDTO;
import ma.femme.entity.Guest;
import ma.femme.entity.Invitation;
import ma.femme.entity.InvitationType;
import ma.femme.repository.InvitationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Consulta administrativa de los mensajes que dejaron los invitados.
// Solo se llama desde /admin/** (protegido con rol ADMIN).
@Service
@RequiredArgsConstructor
public class GuestMessageService {

  static final int DEFAULT_PAGE_SIZE = 10;
  static final int MAX_PAGE_SIZE = 50;

  private final InvitationRepository invitationRepository;

  @Transactional(readOnly = true)
  public GuestMessagePageDTO list(
    int page,
    int size,
    String search,
    InvitationType type,
    GuestMessageFilter filter) {

    GuestMessageFilter effectiveFilter = filter == null ? GuestMessageFilter.WITH_MESSAGE : filter;
    Pageable pageable = PageRequest.of(
      Math.max(page, 0),
      clampSize(size),
      sortFor(effectiveFilter));

    Page<Invitation> result = invitationRepository.findAll(
      buildSpecification(search, type, effectiveFilter),
      pageable);

    long total = invitationRepository.count();
    long withMessage = invitationRepository.countByCoupleMessageIsNotNull();

    return new GuestMessagePageDTO(
      result.getContent().stream().map(GuestMessageDTO::fromEntity).toList(),
      result.getNumber(),
      result.getSize(),
      result.getTotalElements(),
      result.getTotalPages(),
      total,
      withMessage,
      total - withMessage);
  }

  private int clampSize(int size) {
    if (size < 1) {
      return DEFAULT_PAGE_SIZE;
    }
    return Math.min(size, MAX_PAGE_SIZE);
  }

  // Con mensaje: lo más reciente primero (fecha de última actualización).
  // Sin mensaje no hay fecha de mensaje: se ordena por la fecha de creación.
  private Sort sortFor(GuestMessageFilter filter) {
    if (filter == GuestMessageFilter.WITH_MESSAGE) {
      return Sort.by(
        Sort.Order.desc("coupleMessageUpdatedAt"),
        Sort.Order.desc("id"));
    }
    return Sort.by(
      Sort.Order.desc("creationDate"),
      Sort.Order.desc("id"));
  }

  private Specification<Invitation> buildSpecification(
    String search,
    InvitationType type,
    GuestMessageFilter filter) {

    String pattern = likePattern(search);

    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();

      predicates.add(filter == GuestMessageFilter.WITH_MESSAGE
        ? cb.isNotNull(root.get("coupleMessage"))
        : cb.isNull(root.get("coupleMessage")));

      if (type != null) {
        predicates.add(cb.equal(root.get("type"), type));
      }

      if (pattern != null) {
        // Coincide por nombre de la persona/familia, por nombre de alguno
        // de sus invitados o por el contenido del mensaje.
        Subquery<Long> guestMatch = query.subquery(Long.class);
        Root<Guest> guest = guestMatch.from(Guest.class);
        guestMatch.select(guest.<Long>get("id")).where(
          cb.equal(guest.get("invitation"), root),
          cb.like(cb.lower(guest.get("fullName")), pattern, '\\'));

        predicates.add(cb.or(
          cb.like(cb.lower(root.get("referenceName")), pattern, '\\'),
          cb.like(cb.lower(root.get("coupleMessage")), pattern, '\\'),
          cb.exists(guestMatch)));
      }

      return cb.and(predicates.toArray(Predicate[]::new));
    };
  }

  // Patrón LIKE "contiene" en minúsculas; escapa % _ y \ para que lo que
  // escriba el admin se busque literalmente.
  private String likePattern(String search) {
    if (search == null || search.isBlank()) {
      return null;
    }
    String escaped = search.strip().toLowerCase(Locale.ROOT)
      .replace("\\", "\\\\")
      .replace("%", "\\%")
      .replace("_", "\\_");
    return "%" + escaped + "%";
  }
}
