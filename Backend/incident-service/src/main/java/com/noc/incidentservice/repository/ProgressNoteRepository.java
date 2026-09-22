package com.noc.incidentservice.repository;

import com.noc.incidentservice.entity.ProgressNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProgressNoteRepository extends JpaRepository<ProgressNote, Long> {

    List<ProgressNote> findByIncidentIdOrderByCreatedAtDesc(Long incidentId);
}
