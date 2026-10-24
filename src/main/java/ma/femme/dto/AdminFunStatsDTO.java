package ma.femme.dto;

import java.util.List;

// Estadísticas calculadas con consultas a la base de datos. Los tres estados
// son excluyentes y suman totalGuests: notStarted se deriva de los otros dos,
// así un cuestionario nunca cuenta a la vez como pendiente y completado.
public record AdminFunStatsDTO(
    long totalGuests,
    long notStarted,
    long inProgress,
    long completed,
    // Completados / total de invitados, de 0 a 100 con un decimal.
    double completionPercent,
    List<QuestionStat> questions,
    // Solo cuestionarios completados, de más a menos frecuente.
    List<VerdictStat> verdicts) {

  // Cuenta las respuestas guardadas (de cuestionarios completos o en curso).
  public record QuestionStat(String key, String title, long totalAnswers, List<OptionStat> options) {
  }

  public record OptionStat(String key, String label, long count) {
  }

  public record VerdictStat(String key, String headline, long count) {
  }
}
