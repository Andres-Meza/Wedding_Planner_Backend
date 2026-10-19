package ma.femme.service;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ma.femme.dto.GuestFoodRowDTO;
import ma.femme.dto.MenuResponseDTO;
import ma.femme.entity.AllergyType;
import ma.femme.entity.AttendanceStatus;
import ma.femme.repository.GuestRepository;

// Arma la información de alimentación de los invitados: el detalle por
// invitado y los resúmenes (por menú y por alergia) que usan Excel y PDF.
@Service
@RequiredArgsConstructor
public class FoodReportService {

  private final GuestRepository guestRepository;

  public record MenuCount(String menuName, boolean isKids, long count) {
  }

  public record AllergyCount(AllergyType type, long count) {
  }

  // Todos los totales se calculan sobre invitados CONFIRMADOS: son los
  // que realmente se sirven.
  public record FoodSummary(
      long totalConfirmed,
      long totalAdults,
      long totalKids,
      long totalWithAllergies,
      List<MenuCount> menuCounts,
      List<AllergyCount> allergyCounts) {
  }

  @Transactional(readOnly = true)
  public List<GuestFoodRowDTO> getRows() {
    return guestRepository.findAllForFoodReport().stream()
        .map(guest -> GuestFoodRowDTO.builder()
            .guestId(guest.getId())
            .guestName(guest.getFullName())
            .invitationName(guest.getInvitation().getReferenceName())
            .isMinor(guest.isMinor())
            .attendance(guest.getAttendance())
            .menu(MenuResponseDTO.fromEntity(guest.getMenu()))
            .allergyType(guest.getAllergyType())
            .dietaryRestriction(guest.getDietaryRestriction())
            .build())
        .toList();
  }

  public FoodSummary summarize(List<GuestFoodRowDTO> rows) {
    List<GuestFoodRowDTO> confirmed = rows.stream()
        .filter(row -> row.attendance() == AttendanceStatus.CONFIRMED)
        .toList();

    long totalKids = confirmed.stream().filter(GuestFoodRowDTO::isMinor).count();

    List<MenuCount> menuCounts = confirmed.stream()
        .filter(row -> row.menu() != null)
        .collect(Collectors.groupingBy(GuestFoodRowDTO::menu, Collectors.counting()))
        .entrySet().stream()
        .sorted(Comparator
            .comparing((Map.Entry<MenuResponseDTO, Long> e) -> e.getKey().isKids())
            .thenComparing(e -> e.getKey().id()))
        .map(e -> new MenuCount(e.getKey().name(), e.getKey().isKids(), e.getValue()))
        .toList();

    Map<AllergyType, Long> byAllergy = new EnumMap<>(AllergyType.class);
    for (GuestFoodRowDTO row : confirmed) {
      if (row.allergyType() != null) {
        byAllergy.merge(row.allergyType(), 1L, Long::sum);
      }
    }

    List<AllergyCount> allergyCounts = byAllergy.entrySet().stream()
        .map(e -> new AllergyCount(e.getKey(), e.getValue()))
        .toList();

    long withAllergies = allergyCounts.stream().mapToLong(AllergyCount::count).sum();

    return new FoodSummary(
        confirmed.size(),
        confirmed.size() - totalKids,
        totalKids,
        withAllergies,
        menuCounts,
        allergyCounts);
  }
}
