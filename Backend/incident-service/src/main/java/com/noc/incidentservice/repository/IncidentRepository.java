package com.noc.incidentservice.repository;

import com.noc.incidentservice.entity.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface IncidentRepository extends JpaRepository<Incident, Long>, JpaSpecificationExecutor<Incident> {

    /** Used by the SLA-breach scan — all incidents still open, regardless of filters. */
    List<Incident> findByStatusIn(List<String> statuses);
}
