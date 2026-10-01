package ma.femme.entity;

import jakarta.persistence.*;
import lombok.*;

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