package com.noc.dashboardservice;

import com.noc.dashboardservice.client.AlarmClient;
import com.noc.dashboardservice.client.DeviceClient;
import com.noc.dashboardservice.client.IncidentClient;
import com.noc.dashboardservice.util.JwtTestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies JwtAuthFilter + SecurityConfig for dashboard-reporting-service.
 * Every endpoint here aggregates over Feign, so device/alarm/incident
 * clients are mocked with empty results — this suite is testing the
 * security layer, not the aggregation logic itself.
 */
@SpringBootTest
@AutoConfigureMockMvc
class DashboardSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DeviceClient deviceClient;

    @MockBean
    private AlarmClient alarmClient;

    @MockBean
    private IncidentClient incidentClient;

    @BeforeEach
    void stubUpstreamServices() {
        when(deviceClient.getDevices(any(), any(), any(), any())).thenReturn(Collections.emptyList());
        when(alarmClient.getAlarms(any(), any(), any())).thenReturn(Collections.emptyList());
        when(incidentClient.getIncidents(any(), any())).thenReturn(Collections.emptyList());
        when(deviceClient.getByDeviceId(anyString())).thenReturn(null);
    }

    @Test
    void summary_withoutToken_isUnauthorized() throws Exception {
        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void summary_withEngineerRole_isOk() throws Exception {
        String engineerToken = JwtTestUtil.tokenFor("engineer1", "ENGINEER");
        mockMvc.perform(get("/api/dashboard/summary").header("Authorization", "Bearer " + engineerToken))
                .andExpect(status().isOk());
    }

    @Test
    void alarmsBySeverity_withOperatorRole_isOk() throws Exception {
        String opToken = JwtTestUtil.tokenFor("operator1", "NOC_OPERATOR");
        mockMvc.perform(get("/api/dashboard/alarms-by-severity").header("Authorization", "Bearer " + opToken))
                .andExpect(status().isOk());
    }

    @Test
    void incidentsByStatus_withAdminRole_isOk() throws Exception {
        String adminToken = JwtTestUtil.tokenFor("admin1", "ADMIN");
        mockMvc.perform(get("/api/dashboard/incidents-by-status").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void expiredToken_isUnauthorized() throws Exception {
        String expired = JwtTestUtil.expiredTokenFor("engineer1", "ENGINEER");
        mockMvc.perform(get("/api/dashboard/summary").header("Authorization", "Bearer " + expired))
                .andExpect(status().isUnauthorized());
    }
}
