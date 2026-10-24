package ma.femme.export;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import ma.femme.dto.AdminFunAnswerDTO;
import ma.femme.dto.AdminFunGuestDTO;
import ma.femme.dto.AdminFunGuestDetailDTO;
import ma.femme.dto.FunQuizState;
import ma.femme.entity.AttendanceStatus;
import ma.femme.entity.InvitationType;
import ma.femme.service.FunQuizCatalog;

class FunQuizCsvExporterTest {

  private static AdminFunGuestDetailDTO row(String guestName, String invitationName) {
    AdminFunGuestDTO guest = new AdminFunGuestDTO(
        1L, guestName, 10L, invitationName, InvitationType.FAMILY, UUID.randomUUID(),
        UUID.randomUUID(), AttendanceStatus.CONFIRMED, FunQuizState.COMPLETED, 1, 4,
        Instant.parse("2026-10-24T14:00:00Z"), Instant.parse("2026-10-24T14:05:00Z"),
        Instant.parse("2026-10-24T14:05:00Z"), "WHITE_DRESS", "Vestido blanco");
    AdminFunAnswerDTO answer = new AdminFunAnswerDTO(
        "attend", "¿Quieres venir a nuestra boda?", "outfit",
        "¡Sí! Ya estoy buscando qué ponerme.", Instant.parse("2026-10-24T14:01:00Z"));
    return new AdminFunGuestDetailDTO(guest, List.of(answer), null);
  }

  private static String csv(AdminFunGuestDetailDTO... rows) {
    return new String(
        FunQuizCsvExporter.write(List.of(rows), FunQuizCatalog.QUESTIONS), StandardCharsets.UTF_8);
  }

  @Test
  void writesBomHeaderAndOneLinePerGuest() {
    String content = csv(row("Juan Pérez", "Familia Pérez"));

    assertThat(content).startsWith("\uFEFF");
    assertThat(content.split("\r\n")).hasSize(2);
    assertThat(content).contains("Juan Pérez;Confirmada;Completado");
    assertThat(content).contains("¡Sí! Ya estoy buscando qué ponerme.");
  }

  @Test
  void escapesSeparatorsAndQuotes() {
    assertThat(csv(row("Juan", "Familia; \"Pérez\""))).contains("\"Familia; \"\"Pérez\"\"\"");
  }

  @Test
  void neutralizesSpreadsheetFormulas() {
    assertThat(csv(row("=HYPERLINK(\"http://x\")", "Familia"))).contains("'=HYPERLINK");
  }
}
