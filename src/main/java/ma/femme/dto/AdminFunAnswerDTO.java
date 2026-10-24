package ma.femme.dto;

import java.time.Instant;

// Respuesta ya resuelta contra el catálogo: trae el texto de la pregunta y de
// la opción para mostrarla tal cual, además de las claves guardadas.
public record AdminFunAnswerDTO(
    String questionKey,
    String question,
    String optionKey,
    String option,
    Instant answeredAt) {
}
