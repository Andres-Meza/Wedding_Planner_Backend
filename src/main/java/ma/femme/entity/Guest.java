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

    @Column(name = "dietary_restriction")
    private String dietaryRestriction;

    @Column(name = "response_date")
    private LocalDateTime responseDate;
}