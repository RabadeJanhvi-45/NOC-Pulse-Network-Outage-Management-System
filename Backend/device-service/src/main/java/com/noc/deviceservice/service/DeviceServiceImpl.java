package com.noc.deviceservice.service;

import com.noc.deviceservice.dto.DeviceHistoryResponse;
import com.noc.deviceservice.dto.DeviceRequest;
import com.noc.deviceservice.dto.DeviceResponse;
import com.noc.deviceservice.entity.Device;
import com.noc.deviceservice.entity.DeviceHistory;
import com.noc.deviceservice.entity.DeviceRegistrationRequest;
import com.noc.deviceservice.exception.DuplicateDeviceException;
import com.noc.deviceservice.exception.ResourceNotFoundException;
import com.noc.deviceservice.repository.DeviceHistoryRepository;
import com.noc.deviceservice.repository.DeviceRegistrationRequestRepository;
import com.noc.deviceservice.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {

    private static final Set<String> VALID_STATUSES = Set.of("Active", "Inactive", "Faulty");
    private static final Set<String> VALID_HEALTHS = Set.of("Healthy", "Degraded", "Critical");
    private static final String INACTIVE = "Inactive";

    private final DeviceRepository deviceRepository;
    private final DeviceHistoryRepository deviceHistoryRepository;
    private final DeviceRegistrationRequestRepository deviceRegistrationRequestRepository;

    @Override
    @Transactional
    public DeviceResponse registerDevice(DeviceRequest request, String changedBy) {
        if (deviceRepository.existsByDeviceIdIgnoreCase(request.getDeviceId())) {
            throw new DuplicateDeviceException(
                    "A device with deviceId '" + request.getDeviceId() + "' already exists");
        }

        String status = defaultOrValidate(request.getStatus(), VALID_STATUSES, "status", "Active");
        String health = defaultOrValidate(request.getHealth(), VALID_HEALTHS, "health", "Healthy");
        String creator = StringUtils.hasText(request.getCreatedBy())
                ? request.getCreatedBy()
                : (StringUtils.hasText(changedBy) ? changedBy : "system");

        Device device = Device.builder()
                .deviceId(request.getDeviceId())
                .deviceType(request.getDeviceType())
                .name(request.getName())
                .ipAddress(request.getIpAddress())
                .location(request.getLocation())
                .region(request.getRegion())
                .status(status)
                .health(health)
                .createdBy(creator)
                .build();

        Device saved = deviceRepository.save(device);
        return toResponse(saved);
    }

    @Override
    public List<DeviceResponse> getDevices(String region, String status, String deviceType, String search,
                                           String callerRole, String callerUsername, String callerUserId) {
        Specification<Device> spec = buildFilterSpec(region, status, deviceType, search);
        return deviceRepository.findAll(spec).stream()
                .filter(d -> canView(d, callerRole, callerUsername, callerUserId))
                .map(this::toResponse)
                .toList();
    }

    private boolean canView(Device d, String role, String username, String userId) {
        // All authenticated users (ADMIN, NOC_OPERATOR, ENGINEER) can view the monitored network device inventory
        return true;
    }

    @Override
    public DeviceResponse getDeviceById(Long id) {
        return toResponse(findDeviceOrThrow(id));
    }

    @Override
    public DeviceResponse getDeviceByBusinessId(String deviceId) {
        Device device = deviceRepository.findByDeviceIdIgnoreCase(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with deviceId: " + deviceId));
        return toResponse(device);
    }

    @Override
    public boolean isDeviceActive(String deviceId) {
        return deviceRepository.findByDeviceIdIgnoreCase(deviceId)
                .map(d -> "Active".equalsIgnoreCase(d.getStatus()))
                .orElse(false);
    }

    /**
     * Direct edit: all fields (name, type, ip, location, region, health, status)
     * are updated directly without requiring admin verification.
     */
    @Override
    @Transactional
    public DeviceResponse updateDevice(Long id, DeviceRequest request, String changedBy) {
        Device device = findDeviceOrThrow(id);

        recordChangeIfDifferent(device.getId(), "name", device.getName(), request.getName(), changedBy);
        if (request.getName() != null) device.setName(request.getName());

        recordChangeIfDifferent(device.getId(), "deviceType", device.getDeviceType(), request.getDeviceType(), changedBy);
        if (request.getDeviceType() != null) device.setDeviceType(request.getDeviceType());

        recordChangeIfDifferent(device.getId(), "ipAddress", device.getIpAddress(), request.getIpAddress(), changedBy);
        if (request.getIpAddress() != null) device.setIpAddress(request.getIpAddress());

        recordChangeIfDifferent(device.getId(), "location", device.getLocation(), request.getLocation(), changedBy);
        if (request.getLocation() != null) device.setLocation(request.getLocation());

        recordChangeIfDifferent(device.getId(), "region", device.getRegion(), request.getRegion(), changedBy);
        if (request.getRegion() != null) device.setRegion(request.getRegion());

        if (StringUtils.hasText(request.getHealth())) {
            String health = defaultOrValidate(request.getHealth(), VALID_HEALTHS, "health", device.getHealth());
            recordChangeIfDifferent(device.getId(), "health", device.getHealth(), health, changedBy);
            device.setHealth(health);
        }

        if (StringUtils.hasText(request.getStatus())) {
            String status = defaultOrValidate(request.getStatus(), VALID_STATUSES, "status", device.getStatus());
            recordChangeIfDifferent(device.getId(), "status", device.getStatus(), status, changedBy);
            device.setStatus(status);
        }

        Device saved = deviceRepository.save(device);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public DeviceResponse applyApprovedFieldChange(Long deviceId, String deviceType, String ipAddress, String changedBy) {
        Device device = findDeviceOrThrow(deviceId);

        if (StringUtils.hasText(deviceType)) {
            recordChangeIfDifferent(device.getId(), "deviceType", device.getDeviceType(), deviceType, changedBy);
            device.setDeviceType(deviceType);
        }
        if (ipAddress != null) {
            recordChangeIfDifferent(device.getId(), "ipAddress", device.getIpAddress(), ipAddress, changedBy);
            device.setIpAddress(ipAddress);
        }

        return toResponse(deviceRepository.save(device));
    }

    @Override
    @Transactional
    public DeviceResponse applyApprovedDeactivation(Long deviceId, String changedBy) {
        Device device = findDeviceOrThrow(deviceId);
        recordChangeIfDifferent(device.getId(), "status", device.getStatus(), INACTIVE, changedBy);
        device.setStatus(INACTIVE);
        return toResponse(deviceRepository.save(device));
    }

    @Override
    @Transactional
    public void deleteDevice(Long id) {
        Device device = findDeviceOrThrow(id);
        deviceRepository.delete(device);
    }

    @Override
    public List<DeviceHistoryResponse> getHistory(Long id) {
        findDeviceOrThrow(id); // 404 if the device itself doesn't exist
        return deviceHistoryRepository.findByDeviceIdOrderByChangedAtDesc(id).stream()
                .map(this::toHistoryResponse)
                .toList();
    }

    // ---- helpers ----

    private Device findDeviceOrThrow(Long id) {
        return deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with id: " + id));
    }

    private void createChangeRequest(String requestType, Device device, String deviceType, String ipAddress, String requestedBy) {
        DeviceRegistrationRequest changeRequest = DeviceRegistrationRequest.builder()
                .requestType(requestType)
                .targetDeviceId(device.getId())
                .deviceId(device.getDeviceId())
                .deviceType(deviceType)
                .ipAddress(ipAddress)
                .requestedBy(requestedBy)
                .status("PENDING")
                .build();
        deviceRegistrationRequestRepository.save(changeRequest);
    }

    private String defaultOrValidate(String value, Set<String> allowed, String fieldName, String fallback) {
        if (!StringUtils.hasText(value)) {
            return fallback;
        }
        boolean valid = allowed.stream().anyMatch(a -> a.equalsIgnoreCase(value));
        if (!valid) {
            throw new IllegalArgumentException(
                    fieldName + " must be one of " + allowed + " but was '" + value + "'");
        }
        return value;
    }

    private void recordChangeIfDifferent(Long deviceId, String field, String oldValue, String newValue, String changedBy) {
        if (newValue == null || newValue.equals(oldValue)) {
            return;
        }
        DeviceHistory history = DeviceHistory.builder()
                .deviceId(deviceId)
                .changedField(field)
                .oldValue(oldValue)
                .newValue(newValue)
                .changedBy(StringUtils.hasText(changedBy) ? changedBy : "system")
                .build();
        deviceHistoryRepository.save(history);
    }

    private Specification<Device> buildFilterSpec(String region, String status, String deviceType, String search) {
        return (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(region)) {
                predicates.add(cb.equal(cb.lower(root.get("region")), region.toLowerCase()));
            }
            if (StringUtils.hasText(status)) {
                predicates.add(cb.equal(cb.lower(root.get("status")), status.toLowerCase()));
            }
            if (StringUtils.hasText(deviceType)) {
                predicates.add(cb.equal(cb.lower(root.get("deviceType")), deviceType.toLowerCase()));
            }
            if (StringUtils.hasText(search)) {
                String like = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), like),
                        cb.like(cb.lower(root.get("deviceId")), like),
                        cb.like(cb.lower(root.get("location")), like)
                ));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    private DeviceResponse toResponse(Device d) {
        return DeviceResponse.builder()
                .id(d.getId())
                .deviceId(d.getDeviceId())
                .deviceType(d.getDeviceType())
                .name(d.getName())
                .ipAddress(d.getIpAddress())
                .location(d.getLocation())
                .region(d.getRegion())
                .status(d.getStatus())
                .health(d.getHealth())
                .createdBy(d.getCreatedBy())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }

    private DeviceHistoryResponse toHistoryResponse(DeviceHistory h) {
        return DeviceHistoryResponse.builder()
                .id(h.getId())
                .deviceId(h.getDeviceId())
                .changedField(h.getChangedField())
                .oldValue(h.getOldValue())
                .newValue(h.getNewValue())
                .changedBy(h.getChangedBy())
                .changedAt(h.getChangedAt())
                .build();
    }
}