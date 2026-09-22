package com.noc.incidentservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.noc.incidentservice.client.DeviceServiceClient;
import com.noc.incidentservice.client.NotificationServiceClient;
import com.noc.incidentservice.dto.DeviceResponse;
import com.noc.incidentservice.dto.CreateIncidentFromAlarmRequest;
import com.noc.incidentservice.service.AssignmentEngine;
import com.noc.incidentservice.util.JwtTestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Verifies JwtAuthFilter + SecurityConfig for incident-service. DeviceServiceClient
 * is mocked (via @MockBean) rather than pointed at a running device-service — this
 * suite is testing incident-service's own security layer, not the Feign chain.
 */
@SpringBootTest
@AutoConfigureMockMvc
class IncidentSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DeviceServiceClient deviceServiceClient;
    
    @MockBean
    private AssignmentEngine assignmentEngine;
    
    @MockBean
    private NotificationServiceClient notificationServiceClient;

    @BeforeEach
    void stubDeviceLookup() {
        DeviceResponse device = new DeviceResponse();
        device.setId(1L);
        device.setDeviceId("DEV-1");
        device.setStatus("Active");
        when(deviceServiceClient.getDeviceByBusinessId(anyString())).thenReturn(device);
    }

    private CreateIncidentFromAlarmRequest sampleIncident() {
        CreateIncidentFromAlarmRequest request = new CreateIncidentFromAlarmRequest();
        request.setDeviceId("DEV-1");
        request.setAlarmId(1L);
        request.setPriority("P1");
        request.setDescription("Link down on core router");
        return request;
    }

    @Test
    void listIncidents_withoutToken_isUnauthorized() throws Exception {
        mockMvc.perform(get("/api/incidents"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listIncidents_withEngineerRole_isOk() throws Exception {
        String engineerToken = JwtTestUtil.tokenFor("engineer1", "ENGINEER");
        mockMvc.perform(get("/api/incidents").header("Authorization", "Bearer " + engineerToken))
                .andExpect(status().isOk());
    }

    @Test
    void createIncident_withEngineerRole_isForbidden() throws Exception {
        String engineerToken = JwtTestUtil.tokenFor("engineer1", "ENGINEER");
        mockMvc.perform(post("/api/incidents/internal/from-alarm")
                        .header("Authorization", "Bearer " + engineerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleIncident())))
                .andExpect(status().isForbidden());
    }

    @Test
    void createIncident_withOperatorRole_isCreated() throws Exception {
        String opToken = JwtTestUtil.tokenFor("operator1", "NOC_OPERATOR");
        mockMvc.perform(post("/api/incidents/internal/from-alarm")
                        .header("Authorization", "Bearer " + opToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleIncident())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.deviceId").value("DEV-1"));
    }

    @Test
        void createIncident_withAdminRole_isForbidden() throws Exception {
        String adminToken = JwtTestUtil.tokenFor("admin1", "ADMIN");
        mockMvc.perform(post("/api/incidents")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleIncident())))
            .andExpect(status().isForbidden());
    }
}
