package ma.femme.dto;

import java.util.List;

import ma.femme.service.FunQuizCatalog;

// subtitle puede ser null (no todas las preguntas lo tienen).
public record FunQuestionDTO(String key, String title, String subtitle, List<FunOptionDTO> options) {

  public static FunQuestionDTO fromCatalog(FunQuizCatalog.Question question) {
    return new FunQuestionDTO(
        question.key(),
        question.title(),
        question.subtitle(),
        question.options().stream()
            .map(option -> new FunOptionDTO(option.key(), option.label()))
            .toList());
  }
}
