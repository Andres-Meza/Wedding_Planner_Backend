package ma.femme.repository;

import ma.femme.entity.AttendanceStatus;
import ma.femme.entity.Guest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GuestRepository extends JpaRepository<Guest, Long> {

    List<Guest> findByInvitationId(Long invitationId);

    List<Guest> findByAttendance(AttendanceStatus attendance);
}