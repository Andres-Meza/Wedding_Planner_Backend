package ma.femme.dto;

// Resultado de la consulta de distribución de respuestas (pregunta, opción, total).
public record FunAnswerCount(String questionKey, String optionKey, Long total) {
}
