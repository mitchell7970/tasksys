package dev.tasksys.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.tasksys.TasksysApplication;
import dev.tasksys.model.AuthDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = TasksysApplication.class)
@AutoConfigureWebMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class SecurityIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
    }

    @Test
    void shouldAllowAccessToPublicEndpoints() throws Exception {
        // Authentication endpoints should be public
        AuthDto.RegisterRequest registerRequest = new AuthDto.RegisterRequest("publictest", "public@example.com", "password123");

        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(registerRequest))).andExpect(status().isOk());

        AuthDto.LoginRequest loginRequest = new AuthDto.LoginRequest("publictest", "password123");

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(loginRequest))).andExpect(status().isOk());

        // Swagger UI should be accessible
        mockMvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());

        // API docs should be accessible
        mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
    }

    @Test
    void shouldBlockProtectedEndpointsWithoutAuth() throws Exception {
        // All task endpoints should require authentication
        mockMvc.perform(get("/api/tasks")).andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/tasks/1")).andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectInvalidJwtTokens() throws Exception {
        // Invalid token format
        mockMvc.perform(get("/api/tasks").header("Authorization", "InvalidToken")).andExpect(status().isUnauthorized());

        // Malformed Bearer token
        mockMvc.perform(get("/api/tasks").header("Authorization", "Bearer")).andExpect(status().isUnauthorized());

        // Invalid JWT
        mockMvc.perform(get("/api/tasks").header("Authorization", "Bearer invalid.jwt.token")).andExpect(status().isUnauthorized());
    }

    @Test
    void shouldHandleAuthenticationErrors() throws Exception {
        // Invalid credentials
        AuthDto.LoginRequest invalidLogin = new AuthDto.LoginRequest("nonexistent", "wrongpassword");

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(invalidLogin))).andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").value("Invalid username or password"));

        // Duplicate username
        AuthDto.RegisterRequest registerRequest1 = new AuthDto.RegisterRequest("duplicate", "user1@example.com", "password123");
        AuthDto.RegisterRequest registerRequest2 = new AuthDto.RegisterRequest("duplicate", "user2@example.com", "password123");

        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(registerRequest1))).andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(registerRequest2))).andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").value("Username already exists"));
    }

    @Test
    void shouldValidateRequestBodies() throws Exception {
        // Invalid registration data
        AuthDto.RegisterRequest invalidRegister = new AuthDto.RegisterRequest("", "invalid-email", "123");

        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(invalidRegister))).andExpect(status().isBadRequest()).andExpect(jsonPath("$.username").exists()).andExpect(jsonPath("$.email").exists()).andExpect(jsonPath("$.password").exists());

        // Invalid login data
        AuthDto.LoginRequest invalidLogin = new AuthDto.LoginRequest("", "");

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(invalidLogin))).andExpect(status().isBadRequest()).andExpect(jsonPath("$.username").exists()).andExpect(jsonPath("$.password").exists());
    }

    @Test
    void shouldHandleCorsRequests() throws Exception {
        // OPTIONS request should be allowed
        mockMvc.perform((RequestBuilder) options("/api/auth/login").header("Origin", "http://localhost:3000").header("Access-Control-Request-Method", "POST").header("Access-Control-Request-Headers", "content-type")).andExpect(status().isOk());
    }

    @Test
    void shouldReturnProperErrorFormats() throws Exception {
        // Unauthorized should return JSON error
        mockMvc.perform(get("/api/tasks")).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.error").exists());

        // Bad request should return JSON error
        AuthDto.LoginRequest invalidLogin = new AuthDto.LoginRequest("", "");

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(invalidLogin))).andExpect(status().isBadRequest()).andExpect(jsonPath("$.username").exists());
    }

    @Test
    void shouldEnforcePasswordComplexity() throws Exception {
        // Short password should be rejected
        AuthDto.RegisterRequest weakPassword = new AuthDto.RegisterRequest("testuser", "test@example.com", "123");

        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(weakPassword))).andExpect(status().isBadRequest()).andExpect(jsonPath("$.password").value("Password must be at least 6 characters"));
    }
}