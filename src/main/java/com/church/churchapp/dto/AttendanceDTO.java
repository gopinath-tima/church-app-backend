package com.church.churchapp.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AttendanceDTO {
    private Long id;
    private Long memberId;
    private String firstName;
    private String lastName;
    private String customMemberId;
    private Long eventId;
    private LocalDateTime checkInTime;
}
