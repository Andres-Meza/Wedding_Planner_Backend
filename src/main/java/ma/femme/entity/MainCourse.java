package ma.femme.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Plato fuerte de un menú. No es una entidad ni una tabla aparte: es un
// componente embebido en "menu" que agrupa lo que compone el plato fuerte
// (dos proteínas, un acompañamiento y una ensalada). Si algún día el plato
// fuerte necesita otro componente, se agrega aquí y en una migración.
//
// protein1 siempre es obligatoria. Las demás son opcionales a nivel de datos
// porque los menús infantiles pueden no llevarlas; la regla "un menú de
// adultos las exige todas" vive en MenuService.
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MainCourse {

  @Column(name = "protein_1", nullable = false, length = 200)
  private String protein1;

  @Column(name = "protein_2", length = 200)
  private String protein2;

  @Column(name = "accompaniment", length = 200)
  private String accompaniment;

  @Column(name = "salad", length = 200)
  private String salad;
}
