package ma.femme.service;

import java.util.Map;

// Regla ÚNICA y determinista que convierte las respuestas en un veredicto.
// Depende solo de las respuestas guardadas (nunca de azar ni de la hora), así
// que el mismo conjunto de respuestas siempre da el mismo resultado.
//
// Las reglas se evalúan en este orden y gana la primera que se cumpla:
//   1. Vestido de color blanco               -> WHITE_DRESS  (la broma: No aprobado)
//   2. Más emocionado que los novios         -> MOST_EXCITED
//   3. Llorar desde la entrada               -> DRAMATIC
//   4. Venir por la comida o por la música   -> FOODIE
//   5. Regalar "mis mejores pasos de baile"  -> DANCER
//   6. Cualquier otro color de vestido       -> DRESS_APPROVED
//   7. Cualquier otra combinación            -> GENERAL
//
// Un vestido que no sea blanco siempre queda aprobado: los veredictos 2 a 6
// son todos "Aprobado".
public final class FunQuizResolver {

  private FunQuizResolver() {
  }

  // answers: clave de pregunta -> clave de opción (ya validadas contra el
  // catálogo). Devuelve la clave del veredicto (FunQuizCatalog.VERDICTS).
  public static String resolve(Map<String, String> answers) {
    if (is(answers, FunQuizCatalog.Q_DRESS, FunQuizCatalog.O_DRESS_WHITE)) {
      return FunQuizCatalog.V_WHITE_DRESS;
    }
    if (is(answers, FunQuizCatalog.Q_EXCITEMENT, FunQuizCatalog.O_EXCITEMENT_MORE_THAN_COUPLE)) {
      return FunQuizCatalog.V_MOST_EXCITED;
    }
    if (is(answers, FunQuizCatalog.Q_TEARS, FunQuizCatalog.O_TEARS_FROM_ENTRANCE)) {
      return FunQuizCatalog.V_DRAMATIC;
    }
    if (is(answers, FunQuizCatalog.Q_ATTEND, FunQuizCatalog.O_ATTEND_FOOD)
        || is(answers, FunQuizCatalog.Q_EXCITEMENT, FunQuizCatalog.O_EXCITEMENT_MUSIC_FOOD)) {
      return FunQuizCatalog.V_FOODIE;
    }
    if (is(answers, FunQuizCatalog.Q_GIFT, FunQuizCatalog.O_GIFT_DANCE)) {
      return FunQuizCatalog.V_DANCER;
    }
    if (answers.containsKey(FunQuizCatalog.Q_DRESS)) {
      return FunQuizCatalog.V_DRESS_APPROVED;
    }
    return FunQuizCatalog.V_GENERAL;
  }

  private static boolean is(Map<String, String> answers, String questionKey, String optionKey) {
    return optionKey.equals(answers.get(questionKey));
  }
}
