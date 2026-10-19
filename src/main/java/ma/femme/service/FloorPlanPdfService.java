package ma.femme.service;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ma.femme.dto.AssignedGuestDTO;
import ma.femme.dto.TableWithGuestsDTO;
import ma.femme.entity.AttendanceStatus;
import ma.femme.entity.Guest;
import ma.femme.export.FloorPlanPdfExporter;
import ma.femme.export.FloorPlanPdfExporter.GuestLine;
import ma.femme.export.FloorPlanPdfExporter.TableBlock;
import ma.femme.export.FloorPlanPdfExporter.UnseatedLine;
import ma.femme.repository.GuestRepository;

@Service
@RequiredArgsConstructor
public class FloorPlanPdfService {

  // Un PNG del plano pesa algunos cientos de KB; más de esto no es un plano.
  private static final int MAX_IMAGE_BYTES = 10 * 1024 * 1024;
  private static final byte[] PNG_SIGNATURE = { (byte) 0x89, 'P', 'N', 'G' };

  private final TableAssignmentService tableAssignmentService;
  private final GuestRepository guestRepository;
  private final FloorPlanPdfExporter exporter;

  @Transactional(readOnly = true)
  public byte[] buildPdf(String imageBase64) {
    byte[] image = decodePng(imageBase64);

    Map<Long, Guest> guestsById = new HashMap<>();
    for (Guest guest : guestRepository.findAllForFoodReport()) {
      guestsById.put(guest.getId(), guest);
    }

    List<TableBlock> blocks = new ArrayList<>();
    Set<Long> seatedIds = new HashSet<>();

    for (TableWithGuestsDTO table : tableAssignmentService.getTableLayout()) {
      blocks.add(new TableBlock(
          table.nameOrNumber(),
          table.capacity(),
          linesFor(table, guestsById, seatedIds)));
    }

    List<UnseatedLine> unseated = guestsById.values().stream()
        .filter(guest -> guest.getAttendance() == AttendanceStatus.CONFIRMED)
        .filter(guest -> !seatedIds.contains(guest.getId()))
        .sorted((a, b) -> a.getFullName().compareToIgnoreCase(b.getFullName()))
        .map(guest -> new UnseatedLine(
            guest.getFullName(), menuName(guest), allergyLabel(guest)))
        .toList();

    return exporter.export(image, blocks, unseated);
  }

  // Mismo criterio que el plano en pantalla: quien tiene silla fija va en
  // ella y el resto se acomoda en las sillas libres, en orden.
  private List<GuestLine> linesFor(
      TableWithGuestsDTO table, Map<Long, Guest> guestsById, Set<Long> seatedIds) {

    int capacity = table.capacity();
    AssignedGuestDTO[] seats = new AssignedGuestDTO[capacity];
    List<AssignedGuestDTO> floating = new ArrayList<>();

    for (AssignedGuestDTO assigned : table.guests()) {
      Integer seat = assigned.seatIndex();
      if (seat != null && seat < capacity && seats[seat] == null) {
        seats[seat] = assigned;
      } else {
        floating.add(assigned);
      }
    }

    int next = 0;
    for (AssignedGuestDTO assigned : floating) {
      while (next < capacity && seats[next] != null) {
        next++;
      }
      if (next < capacity) {
        seats[next] = assigned;
      }
    }

    List<GuestLine> lines = new ArrayList<>();
    for (int i = 0; i < capacity; i++) {
      AssignedGuestDTO assigned = seats[i];
      if (assigned == null) {
        continue;
      }
      seatedIds.add(assigned.guestId());
      Guest guest = guestsById.get(assigned.guestId());
      lines.add(new GuestLine(
          String.valueOf(i + 1),
          assigned.fullName(),
          guest == null ? "-" : menuName(guest),
          guest == null ? "" : allergyLabel(guest)));
    }
    return lines;
  }

  private String menuName(Guest guest) {
    return guest.getMenu() == null ? "-" : guest.getMenu().getName();
  }

  // "Maní" o "Maní — sin frutos secos". Vacío si no tiene alergia.
  private String allergyLabel(Guest guest) {
    if (guest.getAllergyType() == null) {
      return "";
    }
    String observations = guest.getDietaryRestriction();
    return observations == null || observations.isBlank()
        ? guest.getAllergyType().getLabel()
        : guest.getAllergyType().getLabel() + " — " + observations.trim();
  }

  private byte[] decodePng(String imageBase64) {
    String data = imageBase64;
    int comma = data.indexOf(',');
    if (data.startsWith("data:") && comma >= 0) {
      data = data.substring(comma + 1);
    }

    byte[] bytes;
    try {
      bytes = Base64.getMimeDecoder().decode(data);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("La imagen del plano no es válida");
    }

    boolean isPng = bytes.length > PNG_SIGNATURE.length;
    for (int i = 0; isPng && i < PNG_SIGNATURE.length; i++) {
      isPng = bytes[i] == PNG_SIGNATURE[i];
    }

    if (!isPng || bytes.length > MAX_IMAGE_BYTES) {
      throw new IllegalArgumentException("La imagen del plano debe ser un PNG de menos de 10 MB");
    }
    return bytes;
  }
}
