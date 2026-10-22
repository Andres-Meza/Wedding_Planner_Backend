package ma.femme.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "table_assignment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TableAssignment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // A guest can only have one table assignment (one-to-one relationship).
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "guest_id", nullable = false, unique = true)
  private Guest guest;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "table_id", nullable = false)
  private TableWedding tableWedding;

  // Silla concreta dentro de la mesa (0 = primera). null = sin silla fija.
  @Column(name = "seat_index")
  private Integer seatIndex;

  @Column(name = "manually_assigned", nullable = false)
  @Builder.Default
  private boolean manuallyAssigned = false;
}
