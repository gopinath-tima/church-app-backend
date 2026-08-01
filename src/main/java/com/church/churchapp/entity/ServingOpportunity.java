package com.church.churchapp.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "serving_opportunities")
public class ServingOpportunity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String roleName;
    private String team; // e.g., "Worship", "Welcome", "Tech"
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    
    private int spotsTotal;
    private int spotsFilled;
    
    private String status; // "OPEN", "FILLED"
}
