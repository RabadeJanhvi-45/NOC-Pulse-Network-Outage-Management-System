package com.noc.incidentservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * One row per incident. slaThreshold is set from the priority-based default
 * at creation time; resolutionTime and isBreached are updated as the
 * incident progresses (on close, and by the scheduled breach scan).
 */
@Entity
@Table(name = "sla_tracking")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlaTracking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "incident_id", nullable = false, unique = true)
    private Long incidentId;

    /** Minutes allowed to resolve, derived from priority at creation time. */
    @Column(name = "sla_threshold", nullable = false)
    private Integer slaThreshold;

    /** Minutes actually taken so far (or at close). Updated as the incident progresses. */
    @Column(name = "resolution_time")
    private Integer resolutionTime;

    @Column(name = "is_breached", nullable = false)
    @Builder.Default
    private Boolean isBreached = false;
}
