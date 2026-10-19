package ma.femme.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "table_wedding")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TableWedding {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "name_or_number", nullable = false)
  private String nameOrNumber;

  @Column(nullable = false)
  private Integer capacity;

  @Column
  private String area;
}