package com.noc.alarmservice.repository;

import com.noc.alarmservice.entity.Alarm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface AlarmRepository extends JpaRepository<Alarm, Long>, JpaSpecificationExecutor<Alarm> {

    /** Used to dedupe: is there already an Active alarm for this fault? */
    Optional<Alarm> findFirstByGroupKeyAndStatus(String groupKey, String status);
}
