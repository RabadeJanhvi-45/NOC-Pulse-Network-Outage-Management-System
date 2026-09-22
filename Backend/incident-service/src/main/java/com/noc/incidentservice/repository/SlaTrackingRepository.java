package com.noc.incidentservice.repository;

import com.noc.incidentservice.entity.SlaTracking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SlaTrackingRepository extends JpaRepository<SlaTracking, Long> {

    Optional<SlaTracking> findByIncidentId(Long incidentId);
}
