package com.noc.incidentservice.repository;

import com.noc.incidentservice.entity.IncidentHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncidentHistoryRepository extends JpaRepository<IncidentHistory, Long> {

    List<IncidentHistory> findByIncidentIdOrderByChangedAtDesc(Long incidentId);
}
