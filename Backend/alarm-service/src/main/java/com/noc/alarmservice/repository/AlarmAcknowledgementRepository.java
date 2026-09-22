package com.noc.alarmservice.repository;

import com.noc.alarmservice.entity.AlarmAcknowledgement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlarmAcknowledgementRepository extends JpaRepository<AlarmAcknowledgement, Long> {

    List<AlarmAcknowledgement> findByAlarmIdOrderByAcknowledgedAtDesc(Long alarmId);
}
