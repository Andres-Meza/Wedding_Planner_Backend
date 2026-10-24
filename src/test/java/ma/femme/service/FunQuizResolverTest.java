package ma.femme.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

class FunQuizResolverTest {

  private static Map<String, String> answers(
      String attend, String excitement, String tears, String gift) {
    Map<String, String> answers = new LinkedHashMap<>();
    answers.put(FunQuizCatalog.Q_ATTEND, attend);
    answers.put(FunQuizCatalog.Q_EXCITEMENT, excitement);
    answers.put(FunQuizCatalog.Q_TEARS, tears);
    answers.put(FunQuizCatalog.Q_GIFT, gift);
    return answers;
  }

  @Test
  void lookingForAnOutfitGetsTheWhiteDressJoke() {
    assertThat(FunQuizResolver.resolve(answers("outfit", "countdown", "maybe_not", "home")))
        .isEqualTo(FunQuizCatalog.V_WHITE_DRESS);
  }

  @Test
  void rulesAreEvaluatedInPriorityOrder() {
    assertThat(FunQuizResolver.resolve(answers("outfit", "more_than_couple", "from_entrance", "dance")))
        .isEqualTo(FunQuizCatalog.V_WHITE_DRESS);
    assertThat(FunQuizResolver.resolve(answers("obvio", "more_than_couple", "from_entrance", "dance")))
        .isEqualTo(FunQuizCatalog.V_MOST_EXCITED);
    assertThat(FunQuizResolver.resolve(answers("obvio", "countdown", "from_entrance", "dance")))
        .isEqualTo(FunQuizCatalog.V_DRAMATIC);
    assertThat(FunQuizResolver.resolve(answers("food", "countdown", "maybe_not", "dance")))
        .isEqualTo(FunQuizCatalog.V_FOODIE);
    assertThat(FunQuizResolver.resolve(answers("love", "countdown", "maybe_not", "dance")))
        .isEqualTo(FunQuizCatalog.V_DANCER);
  }

  @Test
  void musicAndFoodExpectationsAlsoGiveTheFoodieVerdict() {
    assertThat(FunQuizResolver.resolve(answers("love", "music_food", "maybe_not", "home")))
        .isEqualTo(FunQuizCatalog.V_FOODIE);
  }

  @Test
  void otherCombinationsGetTheGeneralVerdict() {
    assertThat(FunQuizResolver.resolve(answers("love", "processing", "gossip", "cash")))
        .isEqualTo(FunQuizCatalog.V_GENERAL);
    assertThat(FunQuizResolver.resolve(Map.of())).isEqualTo(FunQuizCatalog.V_GENERAL);
  }

  @Test
  void theSameAnswersAlwaysGiveTheSameVerdict() {
    Set<String> results = new HashSet<>();
    for (int i = 0; i < 100; i++) {
      results.add(FunQuizResolver.resolve(answers("agenda", "hiding_it", "fake_eye", "transfer")));
    }
    assertThat(results).hasSize(1);
  }

  @Test
  void everyPossibleCombinationResolvesToAVerdictInTheCatalog() {
    for (var attend : FunQuizCatalog.QUESTIONS.get(0).options()) {
      for (var excitement : FunQuizCatalog.QUESTIONS.get(1).options()) {
        for (var tears : FunQuizCatalog.QUESTIONS.get(2).options()) {
          for (var gift : FunQuizCatalog.QUESTIONS.get(3).options()) {
            String verdict = FunQuizResolver.resolve(
                answers(attend.key(), excitement.key(), tears.key(), gift.key()));
            assertThat(FunQuizCatalog.findVerdict(verdict)).isPresent();
          }
        }
      }
    }
  }
}
