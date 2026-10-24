package ma.femme.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

class FunAnswerRequestValidationTest {

  private static Validator validator;

  @BeforeAll
  static void createValidator() {
    validator = Validation.buildDefaultValidatorFactory().getValidator();
  }

  @Test
  void aNormalOptionIsValid() {
    assertThat(validator.validate(new FunAnswerRequest("outfit"))).isEmpty();
  }

  @Test
  void blankOrOversizedOptionsAreRejected() {
    assertThat(validator.validate(new FunAnswerRequest(null))).hasSize(1);
    assertThat(validator.validate(new FunAnswerRequest("  "))).hasSize(1);
    assertThat(validator.validate(new FunAnswerRequest("a".repeat(41)))).hasSize(1);
  }
}
