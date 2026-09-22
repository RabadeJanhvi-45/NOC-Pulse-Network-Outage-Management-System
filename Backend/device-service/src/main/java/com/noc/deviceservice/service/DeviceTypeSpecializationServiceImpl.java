package com.noc.deviceservice.service;

import com.noc.deviceservice.dto.DeviceTypeSpecializationRequest;
import com.noc.deviceservice.dto.DeviceTypeSpecializationResponse;
import com.noc.deviceservice.entity.DeviceTypeSpecializationMap;
import com.noc.deviceservice.exception.DuplicateResourceException;
import com.noc.deviceservice.exception.ResourceNotFoundException;
import com.noc.deviceservice.repository.DeviceTypeSpecializationMapRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DeviceTypeSpecializationServiceImpl implements DeviceTypeSpecializationService {

    private final DeviceTypeSpecializationMapRepository repository;

    @Override
    @Transactional
    public DeviceTypeSpecializationResponse create(DeviceTypeSpecializationRequest request) {
        if (repository.existsByDeviceTypeIgnoreCase(request.getDeviceType())) {
            throw new DuplicateResourceException(
                    "A specialization mapping for device type '" + request.getDeviceType() + "' already exists");
        }
        DeviceTypeSpecializationMap saved = repository.save(DeviceTypeSpecializationMap.builder()
                .deviceType(request.getDeviceType())
                .requiredSpecialization(request.getRequiredSpecialization())
                .build());
        return toResponse(saved);
    }

    @Override
    public List<DeviceTypeSpecializationResponse> getAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public DeviceTypeSpecializationResponse update(Long id, DeviceTypeSpecializationRequest request) {
        DeviceTypeSpecializationMap entity = findOrThrow(id);

        if (StringUtils.hasText(request.getDeviceType()) && !request.getDeviceType().equalsIgnoreCase(entity.getDeviceType())
                && repository.existsByDeviceTypeIgnoreCase(request.getDeviceType())) {
            throw new DuplicateResourceException(
                    "A specialization mapping for device type '" + request.getDeviceType() + "' already exists");
        }

        if (StringUtils.hasText(request.getDeviceType())) {
            entity.setDeviceType(request.getDeviceType());
        }
        if (StringUtils.hasText(request.getRequiredSpecialization())) {
            entity.setRequiredSpecialization(request.getRequiredSpecialization());
        }

        return toResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.delete(findOrThrow(id));
    }

    @Override
    public Optional<DeviceTypeSpecializationResponse> findByDeviceType(String deviceType) {
        return repository.findByDeviceTypeIgnoreCase(deviceType).map(this::toResponse);
    }

    // ---- helpers ----

    private DeviceTypeSpecializationMap findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Specialization mapping not found with id: " + id));
    }

    private DeviceTypeSpecializationResponse toResponse(DeviceTypeSpecializationMap m) {
        return DeviceTypeSpecializationResponse.builder()
                .id(m.getId())
                .deviceType(m.getDeviceType())
                .requiredSpecialization(m.getRequiredSpecialization())
                .build();
    }
}