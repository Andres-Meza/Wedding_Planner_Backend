package ma.femme.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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

    // Name of the person who submitted the response:
    // the guest themselves or the person responsible for the invitation.
    @Column(name = "responded_by")
    private String respondedBy;

    // Alergia o restricción alimentaria puntual (ej: "maní", "mariscos").
    @Column(name = "dietary_restriction")
    private String dietaryRestriction;

    // Guarda el nombre del enum elegido: un valor de MenuOption si el
    // invitado es adulto, o de KidsMenuOption si es menor. Se valida en
    // el servicio según corresponda, no a nivel de base de datos, porque
    // una sola columna cubre dos catálogos distintos.
    @Column(name = "selected_menu", length = 50)
    private String selectedMenu;

    @Column(name = "response_date")
    private LocalDateTime responseDate;
}