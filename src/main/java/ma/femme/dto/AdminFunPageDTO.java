package ma.femme.dto;

import java.util.List;

public record AdminFunPageDTO(
    List<AdminFunGuestDTO> content,
    int page,
    int size,
    long totalElements,
    int totalPages) {
}
