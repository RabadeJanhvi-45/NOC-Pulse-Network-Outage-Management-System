package com.noc.incidentservice.repository;

import com.noc.incidentservice.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    List<Assignment> findByIncidentIdOrderByAssignedAtDesc(Long incidentId);

        Optional<Assignment> findFirstByIncidentIdAndIsCurrentTrue(Long incidentId);

    List<Assignment> findByAssigneeIdAndIsCurrentTrue(String assigneeId);

    /**
     * Current assignee -> count of incidents assigned to them whose status
     * is in the given list (used to compute "active tasks" per spec §3:
     * ASSIGNED/IN_PROGRESS only). Engineers with zero active tasks simply
     * don't appear in the result — callers should default missing keys to 0.
     */
    @Query("SELECT a.assigneeId, COUNT(a) FROM Assignment a " +
           "WHERE a.isCurrent = true " +
           "AND a.incidentId IN (SELECT i.id FROM Incident i WHERE i.status IN :statuses) " +
           "GROUP BY a.assigneeId")
    List<Object[]> countActiveByAssignee(@Param("statuses") List<String> statuses);
}