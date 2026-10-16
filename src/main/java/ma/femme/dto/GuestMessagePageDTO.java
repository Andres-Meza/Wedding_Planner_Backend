package ma.femme.dto;

import java.util.List;

// Una página de resultados más los contadores generales (no dependen de
// la búsqueda ni de los filtros aplicados).
public record GuestMessagePageDTO(
  List<GuestMessageDTO> content,
  int page,
  int size,
  long totalElements,
  int totalPages,
  long totalInvitations,
  long withMessageCount,
  long withoutMessageCount
) {
}
