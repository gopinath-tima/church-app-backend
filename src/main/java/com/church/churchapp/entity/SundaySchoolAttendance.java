package com.church.churchapp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sunday_school_attendance")
@Getter
@Setter
public class SundaySchoolAttendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String date;

    @Column(name = "class_id")
    private Long classId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "sunday_school_attendance_students", joinColumns = @JoinColumn(name = "attendance_id"))
    @Column(name = "student_id")
    private List<Long> presentStudentIds = new ArrayList<>();
}
