package com.church.churchapp.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "group_members")
public class GroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long groupMemberId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "group_id", nullable = false)
    private ChurchGroup group;

    private Long memberId;
    private String memberName; // Cached name for convenience
    private String role; // e.g. "Leader", "Co-Leader", "Member"
    private String joinDate; // YYYY-MM-DD
}
