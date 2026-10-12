package ma.femme.entity;

import jakarta.persistence.*;
import lombok.*;

// Una pieza del plano. (x, y) es el CENTRO de la pieza; la rotación va en
// grados alrededor de ese centro.
@Entity
@Table(name = "floor_plan_element")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FloorPlanElement {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private FloorPlanElementKind kind;

  @Column(nullable = false)
  private double x;

  @Column(nullable = false)
  private double y;

  @Column(nullable = false)
  private double width;

  @Column(nullable = false)
  private double height;

  @Column(nullable = false)
  private double rotation;

  @Column(length = 100)
  private String label;

  // Solo para piezas de tipo mesa.
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "table_id", unique = true)
  private TableWedding tableWedding;
}
