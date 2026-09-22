package com.noc.incidentservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Audit trail of reassignments — who handed the incident to whom, and who triggered it. */
@Entity
@Table(name = "assignment_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignmentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "incident_id", nullable = false)
    private Long incidentId;

    /** Null on the very first assignment (no previous assignee). */
    @Column(name = "previous_assignee", length = 100)
    private String previousAssignee;

    @Column(name = "new_assignee", nullable = false, length = 100)
    private String newAssignee;

    @Column(name = "reassigned_by", length = 100)
    private String reassignedBy;

    @Column(name = "reassigned_at", nullable = false, updatable = false)
    private LocalDateTime reassignedAt;

    @PrePersist
    protected void onCreate() {
        this.reassignedAt = LocalDateTime.now();
    }
}
