package com.church.churchapp.service;

import com.church.churchapp.dto.AttendanceDTO;
import com.church.churchapp.entity.Attendance;
import com.church.churchapp.entity.Event;
import com.church.churchapp.entity.Member;
import com.church.churchapp.repository.AttendanceRepository;
import com.church.churchapp.repository.EventRepository;
import com.church.churchapp.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final MemberRepository memberRepository;
    private final EventRepository eventRepository;

    public List<AttendanceDTO> getAttendanceForEvent(Long eventId) {
        return attendanceRepository.findByEvent_EventIdOrderByCheckInTimeDesc(eventId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    public AttendanceDTO checkIn(Long memberId, Long eventId) {
        if(attendanceRepository.existsByMember_MemberIdAndEvent_EventId(memberId, eventId)) {
            throw new RuntimeException("Member is already checked in to this event.");
        }
        
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));
                
        Attendance attendance = new Attendance();
        attendance.setMember(member);
        attendance.setEvent(event);
        attendance.setCheckInTime(LocalDateTime.now());
        
        Attendance saved = attendanceRepository.save(attendance);
        return mapToDTO(saved);
    }
    
    public List<Member> searchMembers(String query) {
        // Try searching by custom member ID (partial match)
        List<Member> byId = memberRepository.findByCustomMemberIdContainingIgnoreCase(query);
        if(!byId.isEmpty()) return byId;
        
        // Simple search combining name/phone capabilities
        List<Member> byName = memberRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(query, query);
        if(!byName.isEmpty()) return byName;
        return memberRepository.findByContactNumberContaining(query);
    }
    
    private AttendanceDTO mapToDTO(Attendance attendance) {
        AttendanceDTO dto = new AttendanceDTO();
        dto.setId(attendance.getId());
        dto.setMemberId(attendance.getMember().getMemberId());
        dto.setFirstName(attendance.getMember().getFirstName());
        dto.setLastName(attendance.getMember().getLastName());
        dto.setCustomMemberId(attendance.getMember().getCustomMemberId());
        dto.setEventId(attendance.getEvent().getEventId());
        dto.setCheckInTime(attendance.getCheckInTime());
        return dto;
    }
}
