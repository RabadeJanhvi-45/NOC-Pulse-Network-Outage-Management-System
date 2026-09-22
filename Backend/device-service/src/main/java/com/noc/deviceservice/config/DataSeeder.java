package com.noc.deviceservice.config;

import com.noc.deviceservice.entity.Device;
import com.noc.deviceservice.entity.DeviceTypeSpecializationMap;
import com.noc.deviceservice.repository.DeviceRepository;
import com.noc.deviceservice.repository.DeviceTypeSpecializationMapRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final DeviceRepository deviceRepository;
    private final DeviceTypeSpecializationMapRepository specializationMapRepository;

    @Override
    public void run(String... args) {
        if (specializationMapRepository.count() == 0) {
            specializationMapRepository.saveAll(List.of(
                    DeviceTypeSpecializationMap.builder().deviceType("Router").requiredSpecialization("Routing & Switching").build(),
                    DeviceTypeSpecializationMap.builder().deviceType("Switch").requiredSpecialization("Routing & Switching").build(),
                    DeviceTypeSpecializationMap.builder().deviceType("Firewall").requiredSpecialization("Firewall & Security").build()
            ));
            log.info("device-service: Seeded default DeviceTypeSpecialization mappings.");
        }

        if (deviceRepository.count() == 0) {
            deviceRepository.saveAll(List.of(
                    Device.builder()
                            .deviceId("DEV-RTR-01")
                            .deviceType("Router")
                            .name("Core Gateway Router")
                            .ipAddress("10.0.0.1")
                            .location("HQ Datacenter Room 101")
                            .region("North")
                            .status("Active")
                            .health("Healthy")
                            .createdBy("system")
                            .build(),
                    Device.builder()
                            .deviceId("DEV-SW-01")
                            .deviceType("Switch")
                            .name("Core Distribution Switch")
                            .ipAddress("10.0.0.10")
                            .location("HQ Datacenter Rack A1")
                            .region("North")
                            .status("Active")
                            .health("Healthy")
                            .createdBy("system")
                            .build(),
                    Device.builder()
                            .deviceId("DEV-FW-01")
                            .deviceType("Firewall")
                            .name("Perimeter Edge Firewall")
                            .ipAddress("10.0.0.2")
                            .location("HQ Datacenter Room 102")
                            .region("North")
                            .status("Active")
                            .health("Healthy")
                            .createdBy("system")
                            .build(),
                    Device.builder()
                            .deviceId("DEV-LB-01")
                            .deviceType("Load Balancer")
                            .name("Application Traffic Balancer")
                            .ipAddress("10.0.0.5")
                            .location("HQ Datacenter Rack B2")
                            .region("North")
                            .status("Active")
                            .health("Healthy")
                            .createdBy("system")
                            .build(),
                    Device.builder()
                            .deviceId("DEV-AP-01")
                            .deviceType("Access Point")
                            .name("Campus Wi-Fi 6 Controller")
                            .ipAddress("10.0.0.25")
                            .location("East Wing Floor 2")
                            .region("East")
                            .status("Active")
                            .health("Healthy")
                            .createdBy("system")
                            .build()
            ));
            log.info("device-service: Seeded default active devices (DEV-RTR-01, DEV-SW-01, DEV-FW-01, DEV-LB-01, DEV-AP-01).");
        }
    }
}
