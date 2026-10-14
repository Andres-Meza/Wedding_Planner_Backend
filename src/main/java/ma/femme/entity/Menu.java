package ma.femme.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// Menú completo (entrada, plato fuerte, postre y bebida). El plato fuerte es
// una sección propia (MainCourse) con sus cuatro componentes. Los menús para
// adultos y para niños comparten estructura; los distingue "kids".
@Entity
@Table(name = "menu")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Menu {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 100)
  private String name;

  @Column(nullable = false, length = 200)
  private String starter;

  // Las columnas protein_1, protein_2, accompaniment y salad de la tabla
  // "menu" las define MainCourse.
  @Embedded
  private MainCourse mainCourse;

  @Column(nullable = false, length = 200)
  private String dessert;

  @Column(nullable = false, length = 200)
  private String drink;

  @Column(name = "is_kids", nullable = false)
  @Builder.Default
  private boolean kids = false;

  // Un menú inactivo ya no se ofrece a los invitados, pero los que lo
  // eligieron antes lo conservan.
  @Column(nullable = false)
  @Builder.Default
  private boolean active = true;

  @Column(name = "created_at", nullable = false, updatable = false)
  @Builder.Default
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(name = "updated_at", nullable = false)
  @Builder.Default
  private LocalDateTime updatedAt = LocalDateTime.now();

  @PreUpdate
  void onUpdate() {
    this.updatedAt = LocalDateTime.now();
  }
}
