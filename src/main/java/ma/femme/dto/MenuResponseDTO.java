package ma.femme.dto;

import lombok.Builder;
import ma.femme.entity.Menu;

@Builder
public record MenuResponseDTO(
        Long id,
        String name,
        String starter,
        MainCourseDTO mainCourse,
        String dessert,
        String drink,
        boolean isKids,
        boolean active
) {

  public static MenuResponseDTO fromEntity(Menu menu) {
    if (menu == null) {
      return null;
    }

    return MenuResponseDTO.builder()
            .id(menu.getId())
            .name(menu.getName())
            .starter(menu.getStarter())
            .mainCourse(MainCourseDTO.fromEntity(menu.getMainCourse()))
            .dessert(menu.getDessert())
            .drink(menu.getDrink())
            .isKids(menu.isKids())
            .active(menu.isActive())
            .build();
  }
}
