package com.church.churchapp.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "ministries")
@Data
public class Ministry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ministryId;

    @Column(unique = true, nullable = false)
    private String ministryName;
}