package com.noc.incidentservice.repository;

import com.noc.incidentservice.entity.Escalation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EscalationRepository extends JpaRepository<Escalation, Long> {

    List<Escalation> findByIncidentIdOrderByEscalatedAtDesc(Long incidentId);
}
