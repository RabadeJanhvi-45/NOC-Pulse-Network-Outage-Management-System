package com.noc.authservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.noc.authservice.config.JwtUtil;
import com.noc.authservice.dto.LoginRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Verifies auth-service's login flow and the JwtAuthFilter + SecurityConfig
 * that now guard everything else. DataSeeder (a CommandLineRunner) seeds a
 * real ADMIN user on startup even against the in-memory test DB, so the
  * "real" login path is exercised directly; ENGINEER/NOC_OPERATOR tokens are
 * minted via the JwtUtil bean itself, mirroring what login would produce.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void login_withSeededAdminCredentials_returnsToken() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("Admin@123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.roleName").value("ADMIN"));
    }

    @Test
    void login_withWrongPassword_isUnauthorized() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("wrong-password");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listUsers_withoutToken_isUnauthorized() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
        void listUsers_withEngineerRole_isForbidden() throws Exception {
                                String engineerToken = jwtUtil.generateToken(99L, "engineer1", "ENGINEER");
                mockMvc.perform(get("/api/users").header("Authorization", "Bearer " + engineerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void listUsers_withAdminRole_isOk() throws Exception {
        // Log in as the seeded admin to get a real, freshly-issued token.
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("Admin@123");

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(loginResponse).get("token").asText();

        mockMvc.perform(get("/api/users").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}
