package com.noc.alarmservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.noc.alarmservice.dto.AlarmRequest;
import com.noc.alarmservice.dto.SeverityRuleRequest;
import com.noc.alarmservice.util.JwtTestUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Verifies JwtAuthFilter + SecurityConfig for alarm-service: no token -> 401,
  * ENGINEER can read but not raise/acknowledge, only ADMIN can write severity
 * rules. alarm.device-validation.enabled=false in test config (see
 * src/test/resources/application.yml) so these tests don't need
 * device-service running.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AlarmSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void listAlarms_withoutToken_isUnauthorized() throws Exception {
        mockMvc.perform(get("/api/alarms"))
                .andExpect(status().isUnauthorized());
    }

    @Test
        void listAlarms_withEngineerRole_isOk() throws Exception {
                                String engineerToken = JwtTestUtil.tokenFor("engineer1", "ENGINEER");
                mockMvc.perform(get("/api/alarms").header("Authorization", "Bearer " + engineerToken))
                .andExpect(status().isOk());
    }

    @Test
        void raiseAlarm_withEngineerRole_isForbidden() throws Exception {
                           String engineerToken = JwtTestUtil.tokenFor("engineer1", "ENGINEER");
        AlarmRequest request = AlarmRequest.builder()
                .deviceId("DEV-1")
                .alarmType("LinkDown")
                .build();

        mockMvc.perform(post("/api/alarms")
                        .header("Authorization", "Bearer " + engineerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void raiseAndAcknowledgeAlarm_withOperatorRole_succeeds() throws Exception {
        String opToken = JwtTestUtil.tokenFor("operator1", "NOC_OPERATOR");
        AlarmRequest request = AlarmRequest.builder()
                .deviceId("DEV-2")
                .alarmType("PowerFailure")
                .severity("Critical")
                .build();

        String raiseResponse = mockMvc.perform(post("/api/alarms")
                        .header("Authorization", "Bearer " + opToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long alarmId = objectMapper.readTree(raiseResponse).get("id").asLong();

        mockMvc.perform(post("/api/alarms/{id}/acknowledge", alarmId)
                        .header("Authorization", "Bearer " + opToken)
                        .header("X-User-Id", "operator1"))
                .andExpect(status().isOk());
    }

    @Test
    void createSeverityRule_withOperatorRole_isForbidden() throws Exception {
        String opToken = JwtTestUtil.tokenFor("operator1", "NOC_OPERATOR");
        SeverityRuleRequest rule = SeverityRuleRequest.builder()
                .alarmType("LinkFlap")
                .severity("Major")
                .build();

        mockMvc.perform(post("/api/alarms/rules")
                        .header("Authorization", "Bearer " + opToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rule)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createSeverityRule_withAdminRole_isOk() throws Exception {
        String adminToken = JwtTestUtil.tokenFor("admin1", "ADMIN");
        SeverityRuleRequest rule = SeverityRuleRequest.builder()
                .alarmType("LinkFlap")
                .severity("Major")
                .build();

        mockMvc.perform(post("/api/alarms/rules")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rule)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.alarmType").value("LinkFlap"));
    }
}
