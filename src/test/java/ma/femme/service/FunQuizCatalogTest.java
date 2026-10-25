package ma.femme.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

// Las claves se guardan en columnas VARCHAR(40) y deben ser únicas: este
// test falla si al ampliar el catálogo se rompe alguna de las dos cosas.
class FunQuizCatalogTest {

  @Test
  void questionAndOptionKeysAreUniqueAndFitTheirColumns() {
    Set<String> questionKeys = new HashSet<>();

    for (FunQuizCatalog.Question question : FunQuizCatalog.QUESTIONS) {
      assertThat(questionKeys.add(question.key())).isTrue();
      assertThat(question.key()).hasSizeLessThanOrEqualTo(40);

      Set<String> optionKeys = new HashSet<>();
      for (FunQuizCatalog.Option option : question.options()) {
        assertThat(optionKeys.add(option.key())).isTrue();
        assertThat(option.key()).hasSizeLessThanOrEqualTo(40);
      }
    }
  }

  @Test
  void hasTheRequestedQuestionsWithTheDressAsTheLastOne() {
    assertThat(FunQuizCatalog.QUESTIONS)
        .extracting(FunQuizCatalog.Question::key)
        .containsExactly("attend", "excitement", "tears", "gift", "dress");
    assertThat(FunQuizCatalog.findQuestion("gift").orElseThrow().options()).hasSize(6);
    assertThat(FunQuizCatalog.findQuestion("dress").orElseThrow().findOption("white")).isPresent();
  }

  @Test
  void verdictKeysAreUniqueAndFitTheirColumn() {
    Set<String> verdictKeys = new HashSet<>();
    for (FunQuizCatalog.Verdict verdict : FunQuizCatalog.VERDICTS) {
      assertThat(verdictKeys.add(verdict.key())).isTrue();
      assertThat(verdict.key()).hasSizeLessThanOrEqualTo(40);
    }
    assertThat(FunQuizCatalog.findVerdict(FunQuizCatalog.V_GENERAL)).isPresent();
  }

  @Test
  void unknownKeysAreNotFound() {
    assertThat(FunQuizCatalog.findQuestion("nope")).isEmpty();
    assertThat(FunQuizCatalog.findQuestion("attend").orElseThrow().findOption("nope")).isEmpty();
    assertThat(FunQuizCatalog.findVerdict("nope")).isEmpty();
  }
}
