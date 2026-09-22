package com.noc.alarmservice.service;

import com.noc.alarmservice.dto.AlarmRequest;
import com.noc.alarmservice.dto.AlarmResponse;

import java.util.List;

public interface AlarmService {

    AlarmResponse raiseAlarm(AlarmRequest request, String callerUsername);

    List<AlarmResponse> getAlarms(String severity, String status, String deviceId, String callerRole, String callerUsername);

    AlarmResponse getAlarmById(Long id);

    AlarmResponse acknowledge(Long id, String acknowledgedBy);

    AlarmResponse clearAlarm(Long id, String clearedBy);
}
