package ma.femme.export;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ma.femme.dto.AdminFunAnswerDTO;
import ma.femme.dto.AdminFunGuestDTO;
import ma.femme.dto.AdminFunGuestDetailDTO;
import ma.femme.dto.FunQuizState;
import ma.femme.entity.AttendanceStatus;
import ma.femme.entity.InvitationType;
import ma.femme.service.FunQuizCatalog;

// Exporta los resultados de la invitación divertida a CSV (UTF-8 con BOM y
// ";" como separador, que es lo que Excel en español abre en columnas sin
// configurar nada). Sin dependencias de Spring: es una función pura.
//
// Las celdas que empiezan por = + - @ se prefijan con una comilla simple para
// que Excel no las interprete como fórmulas (inyección de fórmulas en CSV).
public final class FunQuizCsvExporter {

  private static final char SEPARATOR = ';';
  private static final String LINE_END = "\r\n";
  private static final String BOM = "\uFEFF";

  private FunQuizCsvExporter() {
  }

  public static byte[] write(
      List<AdminFunGuestDetailDTO> rows,
      List<FunQuizCatalog.Question> questions) {

    StringBuilder csv = new StringBuilder(BOM);

    List<String> header = new ArrayList<>(List.of(
        "Invitación (persona o familia)",
        "Tipo de invitación",
        "Invitado",
        "Asistencia (invitación formal)",
        "Estado del cuestionario",
        "Inicio",
        "Última actividad",
        "Finalización",
        "Resultado"));
    questions.forEach(question -> header.add(question.title()));
    appendRow(csv, header);

    for (AdminFunGuestDetailDTO row : rows) {
      AdminFunGuestDTO guest = row.guest();
      Map<String, String> optionByQuestion = row.answers().stream()
          .collect(Collectors.toMap(AdminFunAnswerDTO::questionKey, AdminFunAnswerDTO::option));

      List<String> cells = new ArrayList<>(List.of(
          guest.invitationName(),
          guest.invitationType() == InvitationType.FAMILY ? "Familiar" : "Individual",
          guest.fullName(),
          attendanceLabel(guest.formalAttendance()),
          stateLabel(guest.state()),
          instant(guest.startedAt()),
          instant(guest.lastActivityAt()),
          instant(guest.completedAt()),
          guest.verdictHeadline() == null ? "" : guest.verdictHeadline()));
      questions.forEach(question -> cells.add(optionByQuestion.getOrDefault(question.key(), "")));
      appendRow(csv, cells);
    }

    return csv.toString().getBytes(StandardCharsets.UTF_8);
  }

  // Fecha en ISO-8601 UTC (por ejemplo 2026-10-24T14:20:00Z): inequívoca.
  private static String instant(Instant value) {
    return value == null ? "" : value.toString();
  }

  private static String stateLabel(FunQuizState state) {
    return switch (state) {
      case NOT_STARTED -> "No iniciado";
      case IN_PROGRESS -> "Iniciado";
      case COMPLETED -> "Completado";
    };
  }

  private static String attendanceLabel(AttendanceStatus attendance) {
    return switch (attendance) {
      case NO_RESPONSE -> "Sin respuesta";
      case CONFIRMED -> "Confirmada";
      case NOT_ATTENDING -> "No asiste";
    };
  }

  private static void appendRow(StringBuilder csv, List<String> cells) {
    for (int i = 0; i < cells.size(); i++) {
      if (i > 0) {
        csv.append(SEPARATOR);
      }
      csv.append(escape(cells.get(i)));
    }
    csv.append(LINE_END);
  }

  static String escape(String value) {
    String text = value == null ? "" : value;

    if (!text.isEmpty() && "=+-@\t\r".indexOf(text.charAt(0)) >= 0) {
      text = "'" + text;
    }

    boolean needsQuotes = text.indexOf(SEPARATOR) >= 0
        || text.indexOf('"') >= 0
        || text.indexOf('\n') >= 0
        || text.indexOf('\r') >= 0;

    return needsQuotes ? "\"" + text.replace("\"", "\"\"") + "\"" : text;
  }
}
