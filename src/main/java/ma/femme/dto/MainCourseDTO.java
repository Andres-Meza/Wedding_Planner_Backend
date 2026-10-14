package ma.femme.dto;

import lombok.Builder;
import ma.femme.entity.MainCourse;

// Plato fuerte tal como lo devuelve la API, dentro de MenuResponseDTO.
@Builder
public record MainCourseDTO(
  String protein1,
  String protein2,
  String accompaniment,
  String salad
) {

  public static MainCourseDTO fromEntity(MainCourse mainCourse) {
    if (mainCourse == null) {
      return null;
    }

    return MainCourseDTO.builder()
      .protein1(mainCourse.getProtein1())
      .protein2(mainCourse.getProtein2())
      .accompaniment(mainCourse.getAccompaniment())
      .salad(mainCourse.getSalad())
      .build();
  }
}
