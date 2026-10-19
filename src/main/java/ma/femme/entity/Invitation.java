package ma.femme.entity;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.hibernate.annotations.BatchSize;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "invitation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invitation {

  // Longitud máxima del mensaje para los novios. La usan la columna, la
  // validación del DTO público y el servicio, para que no se desincronicen.
  public static final int COUPLE_MESSAGE_MAX_LENGTH = 1000;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private InvitationType type;

  @Column(name = "unique_token", nullable = false, unique = true, updatable = false)
  @Builder.Default
  private UUID uniqueToken = UUID.randomUUID();

  @Column(name = "reference_name", nullable = false)
  private String referenceName;

  @Column(name = "contact_phone")
  private String contactPhone;

  @Column(name = "contact_email")
  private String contactEmail;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 25)
  @Builder.Default
  private InvitationStatus status = InvitationStatus.PENDING;

  @Column(name = "creation_date", nullable = false, updatable = false)
  @Builder.Default
  private LocalDateTime creationDate = LocalDateTime.now();

  // Mensaje opcional para los novios (texto plano). null = sin mensaje.
  // Es único por invitación: una persona o toda una familia.
  @Column(name = "couple_message", length = COUPLE_MESSAGE_MAX_LENGTH)
  private String coupleMessage;

  // Cuándo se guardó por última vez. Es null si y solo si no hay mensaje.
  @Column(name = "couple_message_updated_at")
  private LocalDateTime coupleMessageUpdatedAt;

  // BatchSize evita una consulta por invitación al listar muchas a la vez
  // (por ejemplo, para contar los invitados de cada mensaje).
  @OneToMany(mappedBy = "invitation", cascade = CascadeType.ALL, orphanRemoval = true)
  @BatchSize(size = 50)
  @Builder.Default
  private List<Guest> guests = new ArrayList<>();

  public void addGuest(Guest guest) {
    guests.add(guest);
    guest.setInvitation(this);
  }

  /**
   * La fecha del mensaje como instante absoluto (null si no hay mensaje).
   * La columna es TIMESTAMP sin zona horaria y se escribe con la hora del
   * servidor; al exponerla como Instant el navegador la convierte a la hora
   * local de quien la ve, sin importar en qué zona corra el servidor.
   */
  public Instant coupleMessageUpdatedAtInstant() {
    return coupleMessageUpdatedAt == null
        ? null
        : coupleMessageUpdatedAt.atZone(ZoneId.systemDefault()).toInstant();
  }

  /**
   * Guarda el mensaje ya normalizado (null = sin mensaje). Si el texto no
   * cambió no toca la fecha, así un reintento o un reenvío idéntico no
   * altera la "última actualización". Devuelve true si hubo cambios.
   */
  public boolean updateCoupleMessage(String normalizedMessage, LocalDateTime now) {
    if (Objects.equals(coupleMessage, normalizedMessage)) {
      return false;
    }
    this.coupleMessage = normalizedMessage;
    this.coupleMessageUpdatedAt = normalizedMessage == null ? null : now;
    return true;
  }
}
