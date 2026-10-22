package ma.femme.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import ma.femme.entity.InvitationType;

// Los límites coinciden con las columnas de la base de datos: un valor
// demasiado largo se rechaza con 400 en vez de fallar al guardar.
class RequestValidationTest {

  private static Validator validator;

  @BeforeAll
  static void createValidator() {
    validator = Validation.buildDefaultValidatorFactory().getValidator();
  }

  @Test
  void aNormalInvitationIsValid() {
    assertThat(validator.validate(invitation("Familia Pérez", "3001234567", "ana@example.com")))
        .isEmpty();
  }

  @Test
  void invitationFieldsAreLimitedToTheirColumnLength() {
    assertThat(properties(invitation("a".repeat(151), null, null))).containsExactly("referenceName");
    assertThat(properties(invitation("Ana", "1".repeat(31), null))).containsExactly("contactPhone");
    // Correo válido (local y etiquetas del dominio cortos) pero de más de 150 caracteres.
    String longEmail = "a@" + ("b".repeat(60) + ".").repeat(3) + "com";
    assertThat(properties(invitation("Ana", null, longEmail))).containsExactly("contactEmail");
  }

  @Test
  void guestNameIsLimitedToItsColumnLength() {
    assertThat(properties(new CreateGuestRequest("a".repeat(151), false))).containsExactly("fullName");
  }

  @Test
  void tableFieldsAreLimitedToTheirColumnLength() {
    assertThat(properties(new CreateTableRequest("a".repeat(51), 8, null)))
        .containsExactly("nameOrNumber");
    assertThat(properties(new CreateTableRequest("A", 8, "z".repeat(101)))).containsExactly("zone");
  }

  @Test
  void floorPlanImageHasAnUpperBound() {
    assertThat(validator.validate(new FloorPlanPdfRequest("a".repeat(1000)))).isEmpty();
    assertThat(properties(new FloorPlanPdfRequest("a".repeat(FloorPlanPdfRequest.MAX_BASE64_LENGTH + 1))))
        .containsExactly("imageBase64");
  }

  private static CreateInvitationRequest invitation(String name, String phone, String email) {
    return new CreateInvitationRequest(
        InvitationType.INDIVIDUAL, name, phone, email,
        List.of(new CreateGuestRequest("Ana Gómez", false)));
  }

  private static <T> Set<String> properties(T request) {
    return validator.validate(request).stream()
        .map(ConstraintViolation::getPropertyPath)
        .map(Object::toString)
        .collect(java.util.stream.Collectors.toSet());
  }
}
