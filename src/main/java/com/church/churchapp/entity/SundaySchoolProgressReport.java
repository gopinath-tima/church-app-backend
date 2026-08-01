package com.church.churchapp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "sunday_school_progress_reports")
@Getter
@Setter
public class SundaySchoolProgressReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id")
    private Long studentId;

    private String date;

    @Column(name = "verses_memorized")
    private Integer versesMemorized;

    private String behavior;

    @Column(length = 2000)
    private String remarks;
}
