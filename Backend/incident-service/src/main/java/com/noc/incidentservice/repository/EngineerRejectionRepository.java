package com.noc.incidentservice.repository;

import com.noc.incidentservice.entity.EngineerRejection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EngineerRejectionRepository extends JpaRepository<EngineerRejection, Long> {

    Optional<EngineerRejection> findFirstByIncidentIdAndAdminDecisionIsNullOrderByRejectedAtDesc(
            Long incidentId
    );

    Optional<EngineerRejection> findFirstByIncidentIdAndEngineerIdAndAdminDecisionIsNull(
            Long incidentId,
            String engineerId
    );

    /** All rejections ever recorded for this incident, used to exclude those Engineers on reassignment. */
    List<EngineerRejection> findByIncidentId(Long incidentId);
}