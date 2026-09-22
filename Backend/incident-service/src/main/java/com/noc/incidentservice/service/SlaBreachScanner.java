package com.noc.incidentservice.service;

import com.noc.incidentservice.entity.Escalation;
import com.noc.incidentservice.entity.Incident;
import com.noc.incidentservice.entity.SlaTracking;
import com.noc.incidentservice.repository.EscalationRepository;
import com.noc.incidentservice.repository.IncidentRepository;
import com.noc.incidentservice.repository.SlaTrackingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled job (guide §5.3): scans open incidents, updates their live
 * resolutionTime, flags isBreached = true once the SLA threshold is
 * exceeded, and auto-raises a system-triggered Escalation the first time a
 * given incident crosses its threshold.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SlaBreachScanner {

    private static final List<String> OPEN_STATUSES = List.of("ASSIGNED", "IN_PROGRESS");

    private final IncidentRepository incidentRepository;
    private final SlaTrackingRepository slaTrackingRepository;
    private final EscalationRepository escalationRepository;

    @Scheduled(fixedDelayString = "${incident.sla-scan.fixed-delay-ms:300000}")
    @Transactional
    public void scanForBreaches() {
        List<Incident> openIncidents = incidentRepository.findByStatusIn(OPEN_STATUSES);
        if (openIncidents.isEmpty()) {
            return;
        }

        int newlyBreached = 0;
        for (Incident incident : openIncidents) {
            SlaTracking sla = slaTrackingRepository.findByIncidentId(incident.getId()).orElse(null);
            if (sla == null) {
                continue;
            }

            long minutesElapsed = Duration.between(incident.getCreatedAt(), LocalDateTime.now()).toMinutes();
            sla.setResolutionTime((int) minutesElapsed);

            boolean crossedThreshold = minutesElapsed > sla.getSlaThreshold();
            boolean wasAlreadyBreached = Boolean.TRUE.equals(sla.getIsBreached());

            if (crossedThreshold && !wasAlreadyBreached) {
                sla.setIsBreached(true);
                slaTrackingRepository.save(sla);

                Escalation escalation = Escalation.builder()
                        .incidentId(incident.getId())
                        .escalatedBy(null) // system-triggered
                        .reason("SLA threshold of " + sla.getSlaThreshold() + " minutes exceeded")
                        .triggerType("sla_breach")
                        .build();
                escalationRepository.save(escalation);
                newlyBreached++;
            } else {
                slaTrackingRepository.save(sla);
            }
        }

        if (newlyBreached > 0) {
            log.info("SLA breach scan: {} incident(s) newly flagged as breached and auto-escalated", newlyBreached);
        }
    }
}
