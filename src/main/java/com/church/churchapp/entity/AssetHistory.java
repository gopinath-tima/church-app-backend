package com.church.churchapp.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "asset_history")
@Data
public class AssetHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long historyId;

    @Column(nullable = false)
    private Long assetId;

    @Column(nullable = false, length = 50)
    private String action; // e.g., "Created", "Updated", "Status Changed", "Maintenance Logged"

    @Column(nullable = false)
    private LocalDateTime actionDate;

    @Column(columnDefinition = "TEXT")
    private String description;
}
