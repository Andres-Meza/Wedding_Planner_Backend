package ma.femme.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "invitation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invitation {
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

    @OneToMany(
            mappedBy = "invitation",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<Guest> guests = new ArrayList<>();

    public void addGuest(Guest guest) {
        guests.add(guest);
        guest.setInvitation(this);
    }
}