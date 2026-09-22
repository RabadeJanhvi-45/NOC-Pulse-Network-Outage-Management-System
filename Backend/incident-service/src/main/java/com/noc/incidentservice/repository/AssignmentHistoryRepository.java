package com.noc.incidentservice.repository;

import com.noc.incidentservice.entity.AssignmentHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssignmentHistoryRepository extends JpaRepository<AssignmentHistory, Long> {

    List<AssignmentHistory> findByIncidentIdOrderByReassignedAtDesc(Long incidentId);
}
