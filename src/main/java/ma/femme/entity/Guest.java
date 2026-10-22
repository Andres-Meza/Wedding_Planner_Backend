package ma.femme.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "guest")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Guest {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "invitation_id", nullable = false)
  private Invitation invitation;

  @Column(name = "full_name", nullable = false)
  private String fullName;

  @Column(name = "is_minor", nullable = false)
  @Builder.Default
  private boolean isMinor = false;

  @Enumerated(EnumType.STRING)
  @Column(name = "attendance", nullable = false, length = 20)
  @Builder.Default
  private AttendanceStatus attendance = AttendanceStatus.NO_RESPONSE;

  // Tipo de alergia alimentaria. null = sin alergias reportadas.
  @Enumerated(EnumType.STRING)
  @Column(name = "allergy_type", length = 20)
  private AllergyType allergyType;

  // Observaciones / restricciones alimentarias en texto libre
  // (ej: "No puede consumir ningún alimento con frutos secos").
  @Column(name = "dietary_restriction")
  private String dietaryRestriction;

  // Menú asignado automáticamente según la categoría del invitado
  // (adulto -> Menú 1, menor -> Menú Infantil 1); ver DefaultMenuService.
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "menu_id")
  private Menu menu;

  @Column(name = "response_date")
  private LocalDateTime responseDate;
}