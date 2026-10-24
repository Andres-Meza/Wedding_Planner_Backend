package ma.femme.service;

import java.util.List;
import java.util.Optional;

// Fuente única de las preguntas, opciones y veredictos de la invitación
// divertida. El frontend dibuja lo que este catálogo devuelve y el backend
// valida las respuestas contra él, así que nunca se desincronizan.
//
// Cómo ampliarlo:
//   * Nueva opción: agregar una línea option("clave", "Texto") a la pregunta.
//   * Nueva pregunta: agregar otro question(...) a QUESTIONS (el cuestionario
//     se completa cuando todas las preguntas del catálogo tienen respuesta).
//   * Nuevo veredicto: agregarlo a VERDICTS y una regla en FunQuizResolver.
//
// Las claves (key) se guardan en la base de datos y deben ser estables: el
// texto se puede reescribir libremente, pero una clave en uso no debe
// renombrarse ni borrarse. Máximo 40 caracteres por clave (columna VARCHAR(40)).
public final class FunQuizCatalog {

  public record Option(String key, String label) {
  }

  public record Question(String key, String title, String subtitle, List<Option> options) {

    public Optional<Option> findOption(String optionKey) {
      return options.stream().filter(option -> option.key().equals(optionKey)).findFirst();
    }
  }

  // approved = false solo cambia el sello ("No aprobado"); es una broma, no
  // una restricción real: nadie pierde su invitación por sus respuestas.
  public record Verdict(String key, String headline, String stamp, boolean approved, String message) {
  }

  // Claves de las preguntas y opciones que usan las reglas del veredicto.
  public static final String Q_ATTEND = "attend";
  public static final String Q_EXCITEMENT = "excitement";
  public static final String Q_TEARS = "tears";
  public static final String Q_GIFT = "gift";

  public static final String O_ATTEND_OUTFIT = "outfit";
  public static final String O_ATTEND_FOOD = "food";
  public static final String O_EXCITEMENT_MORE_THAN_COUPLE = "more_than_couple";
  public static final String O_EXCITEMENT_MUSIC_FOOD = "music_food";
  public static final String O_TEARS_FROM_ENTRANCE = "from_entrance";
  public static final String O_GIFT_DANCE = "dance";

  public static final String V_WHITE_DRESS = "WHITE_DRESS";
  public static final String V_MOST_EXCITED = "MOST_EXCITED";
  public static final String V_DRAMATIC = "DRAMATIC";
  public static final String V_FOODIE = "FOODIE";
  public static final String V_DANCER = "DANCER";
  public static final String V_GENERAL = "GENERAL";

  public static final List<Question> QUESTIONS = List.of(
      question(Q_ATTEND, "¿Quieres venir a nuestra boda?", null,
          option("obvio", "¡Obvio! No me lo perdería por nada."),
          option(O_ATTEND_OUTFIT, "¡Sí! Ya estoy buscando qué ponerme."),
          option("love", "Voy porque los quiero mucho."),
          option("agenda", "Estoy revisando mi agenda, no me presiones."),
          option(O_ATTEND_FOOD, "¿Habrá comida? Es para un amigo.")),

      question(Q_EXCITEMENT, "¿Qué tanto te emociona nuestra boda?", null,
          option(O_EXCITEMENT_MORE_THAN_COUPLE, "Estoy más emocionado que los novios."),
          option("countdown", "Ya quiero que llegue el gran día."),
          option("hiding_it", "Estoy emocionado, pero lo disimulo."),
          option(O_EXCITEMENT_MUSIC_FOOD, "Espero que haya buena música y comida."),
          option("processing", "Todavía estoy procesando la invitación.")),

      question(Q_TEARS, "¿Vas a llorar durante la ceremonia?", null,
          option(O_TEARS_FROM_ENTRANCE, "Sí, voy a llorar desde la entrada."),
          option("if_bride_cries", "Solo si llora la novia."),
          option("maybe_not", "No voy a llorar… creo."),
          option("fake_eye", "Voy a fingir que me entró algo en el ojo."),
          option("gossip", "Yo voy por el chisme emocional.")),

      question(Q_GIFT, "¿Qué vas a traer como regalo de boda?",
          "Es solo por diversión: elegir una opción no te compromete a nada.",
          option("home", "Un regalo para el hogar."),
          option("cash", "Dinero en un sobre."),
          option("handmade", "Un detalle hecho con mucho amor."),
          option(O_GIFT_DANCE, "Mi presencia y mis mejores pasos de baile."),
          option("transfer", "¿También reciben transferencias?"),
          option("thinking", "Todavía estoy pensando en qué regalar.")));

  public static final List<Verdict> VERDICTS = List.of(
      new Verdict(V_WHITE_DRESS, "Vestido blanco", "No aprobado", false,
          "Ese color ya tiene dueña, y es la novia. Tu invitación sigue más firme que nunca: "
              + "escoge cualquier otro tono y todo en orden."),
      new Verdict(V_MOST_EXCITED, "Invitado más emocionado", "Aprobado", true,
          "Tu entusiasmo supera al de los novios, y eso ya es decir mucho. "
              + "Te guardamos un lugar de honor."),
      new Verdict(V_DRAMATIC, "El más dramático", "Pañuelos incluidos", true,
          "Se te humedecen los ojos desde la entrada. "
              + "Te sentamos cerca de los pañuelos."),
      new Verdict(V_FOODIE, "Pendiente de la comida", "Aprobado", true,
          "Tus prioridades están claras y las respetamos: "
              + "habrá buena música, mucho baile y, por supuesto, buena comida."),
      new Verdict(V_DANCER, "Alma de la pista", "Aprobado", true,
          "Tus mejores pasos ya tienen escenario esperándote. "
              + "Ven con zapatos cómodos."),
      new Verdict(V_GENERAL, "Invitado oficial", "Aprobado", true,
          "Respondiste como todo un invitado de lujo. "
              + "Tu lugar en nuestra celebración está más que merecido."));

  private FunQuizCatalog() {
  }

  public static Optional<Question> findQuestion(String questionKey) {
    return QUESTIONS.stream().filter(question -> question.key().equals(questionKey)).findFirst();
  }

  public static Optional<Verdict> findVerdict(String verdictKey) {
    return VERDICTS.stream().filter(verdict -> verdict.key().equals(verdictKey)).findFirst();
  }

  private static Question question(
      String key, String title, String subtitle, Option... options) {
    return new Question(key, title, subtitle, List.of(options));
  }

  private static Option option(String key, String label) {
    return new Option(key, label);
  }
}
