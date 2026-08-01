package com.church.churchapp.repository;

import com.church.churchapp.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByEvent_EventIdOrderByCheckInTimeDesc(Long eventId);
    boolean existsByMember_MemberIdAndEvent_EventId(Long memberId, Long eventId);
}
