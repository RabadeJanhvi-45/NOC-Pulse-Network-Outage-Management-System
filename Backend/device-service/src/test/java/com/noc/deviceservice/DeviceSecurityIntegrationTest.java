package com.noc.deviceservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.noc.deviceservice.dto.DeviceRequest;
import com.noc.deviceservice.util.JwtTestUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Verifies JwtAuthFilter + SecurityConfig actually enforce what they claim
 * to: no token -> 401, wrong role on a write -> 403, right role -> success.
 * Also exercises the core register/list/update flow end-to-end against an
 * in-memory H2 instance (see src/test/resources/application.yml).
 */
@SpringBootTest
@AutoConfigureMockMvc
class DeviceSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private DeviceRequest sampleDevice(String deviceId) {
        return DeviceRequest.builder()
                .deviceId(deviceId)
                .deviceType("Router")
                .name("Core Router " + deviceId)
                .ipAddress("10.0.0.1")
                .location("DC1")
                .region("North")
                .build();
    }

    @Test
    void listDevices_withoutToken_isUnauthorized() throws Exception {
        mockMvc.perform(get("/api/devices"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void listDevices_withAnyValidRole_isOk() throws Exception {
        String engineerToken = JwtTestUtil.tokenFor("engineer1", "ENGINEER");
        mockMvc.perform(get("/api/devices").header("Authorization", "Bearer " + engineerToken))
                .andExpect(status().isOk());
    }

    @Test
        void registerDevice_withEngineerRole_isForbidden() throws Exception {
                String engineerToken = JwtTestUtil.tokenFor("engineer1", "ENGINEER");
        mockMvc.perform(post("/api/devices")
                        .header("Authorization", "Bearer " + engineerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleDevice("DEV-ENGINEER-1"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void registerDevice_withOperatorRole_isCreated() throws Exception {
        String opToken = JwtTestUtil.tokenFor("operator1", "NOC_OPERATOR");
        mockMvc.perform(post("/api/devices")
                        .header("Authorization", "Bearer " + opToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleDevice("DEV-OP-1"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.deviceId").value("DEV-OP-1"));
    }

    @Test
    void registerThenUpdateDevice_withAdminRole_reflectsChange() throws Exception {
        String adminToken = JwtTestUtil.tokenFor("admin1", "ADMIN");

        String createResponse = mockMvc.perform(post("/api/devices")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleDevice("DEV-ADMIN-1"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(createResponse).get("id").asLong();

        DeviceRequest update = sampleDevice("DEV-ADMIN-1");
        update.setStatus("Faulty");

        mockMvc.perform(put("/api/devices/{id}", id)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Faulty"));
    }

    @Test
    void expiredToken_isUnauthorized() throws Exception {
        String expired = JwtTestUtil.expiredTokenFor("admin1", "ADMIN");
        mockMvc.perform(get("/api/devices").header("Authorization", "Bearer " + expired))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void h2Console_isReachableWithoutToken() throws Exception {
        // Not asserting the exact status the H2 console servlet returns (varies
        // by version/config) — the point of this test is that it's NOT 401/403,
        // i.e. permitAll() on /h2-console/** is actually taking effect.
        int status = mockMvc.perform(get("/h2-console"))
                .andReturn().getResponse().getStatus();
        org.assertj.core.api.Assertions.assertThat(status)
                .as("h2-console should not be blocked by JwtAuthFilter/SecurityConfig")
                .isNotIn(401, 403);
    }
}
