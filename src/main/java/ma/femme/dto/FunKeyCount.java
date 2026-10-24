package ma.femme.dto;

// Resultado de una consulta agrupada por clave (clave, total).
public record FunKeyCount(String key, Long total) {
}
