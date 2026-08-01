package com.church.churchapp.controller;

import com.church.churchapp.dto.AttendanceDTO;
import com.church.churchapp.entity.Member;
import com.church.churchapp.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('MINISTRY', 'ADMIN', 'SUPER_ADMIN', 'SUPER_PLUS_ADMIN')")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<AttendanceDTO>> getEventAttendance(@PathVariable Long eventId) {
        return ResponseEntity.ok(attendanceService.getAttendanceForEvent(eventId));
    }
    
    @PostMapping("/checkin")
    public ResponseEntity<?> checkIn(@RequestParam Long memberId, @RequestParam Long eventId) {
        try {
            return ResponseEntity.ok(attendanceService.checkIn(memberId, eventId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<Member>> searchMembers(@RequestParam String query) {
        return ResponseEntity.ok(attendanceService.searchMembers(query));
    }
}
