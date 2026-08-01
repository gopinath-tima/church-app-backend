package com.church.churchapp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "sunday_school_classes")
@Getter
@Setter
public class SundaySchoolClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String room;
    private String time;

    @Column(length = 1000)
    private String description;

    @Column(name = "teacher_id")
    private Long teacherId;

    @Column(name = "event_id")
    private Long eventId;
}
