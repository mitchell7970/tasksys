package dev.tasksys.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.tasksys.TasksysApplication;
import dev.tasksys.model.AuthDto;
import dev.tasksys.model.TaskDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = TasksysApplication.class)
@AutoConfigureWebMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TaskIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private String jwtToken;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();

        // Register and get JWT token for testing
        AuthDto.RegisterRequest registerRequest = new AuthDto.RegisterRequest("testuser", "test@example.com", "password123");

        MvcResult result = mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(registerRequest))).andExpect(status().isOk()).andReturn();

        String response = result.getResponse().getContentAsString();
        AuthDto.AuthResponse authResponse = objectMapper.readValue(response, AuthDto.AuthResponse.class);
        jwtToken = authResponse.getToken();
    }

    @Test
    void shouldPerformFullTaskCRUDOperationsWithAuthentication() throws Exception {
        // Create task
        TaskDto newTask = new TaskDto("Integration Task", "Integration Description", LocalDate.now().plusDays(7), "TO_DO");

        String taskJson = objectMapper.writeValueAsString(newTask);

        String response = mockMvc.perform(post("/api/tasks").header("Authorization", "Bearer " + jwtToken).contentType(MediaType.APPLICATION_JSON).content(taskJson)).andExpect(status().isCreated()).andExpect(jsonPath("$.title").value("Integration Task")).andReturn().getResponse().getContentAsString();

        TaskDto createdTask = objectMapper.readValue(response, TaskDto.class);
        Long taskId = createdTask.getId();

        // Get all tasks (should only return current user's tasks)
        mockMvc.perform(get("/api/tasks").header("Authorization", "Bearer " + jwtToken)).andExpect(status().isOk()).andExpect(jsonPath("$[0].title").value("Integration Task"));

        // Get task by ID
        mockMvc.perform(get("/api/tasks/" + taskId).header("Authorization", "Bearer " + jwtToken)).andExpect(status().isOk()).andExpect(jsonPath("$.title").value("Integration Task"));

        // Update task
        TaskDto updateTask = new TaskDto("Updated Integration Task", "Updated Description", LocalDate.now().plusDays(10), "IN_PROGRESS");

        mockMvc.perform(put("/api/tasks/" + taskId).header("Authorization", "Bearer " + jwtToken).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(updateTask))).andExpect(status().isOk()).andExpect(jsonPath("$.title").value("Updated Integration Task")).andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        // Get tasks by status
        mockMvc.perform(get("/api/tasks/status/IN_PROGRESS").header("Authorization", "Bearer " + jwtToken)).andExpect(status().isOk()).andExpect(jsonPath("$[0].status").value("IN_PROGRESS"));

        // Delete task
        mockMvc.perform(delete("/api/tasks/" + taskId).header("Authorization", "Bearer " + jwtToken)).andExpect(status().isNoContent());

        // Verify task is deleted
        mockMvc.perform(get("/api/tasks/" + taskId).header("Authorization", "Bearer " + jwtToken)).andExpect(status().isNotFound());
    }

    @Test
    void shouldNotAllowAccessWithoutAuthentication() throws Exception {
        TaskDto newTask = new TaskDto("Unauthorized Task", "Description", LocalDate.now().plusDays(7), "TO_DO");

        // Try to create task without token
        mockMvc.perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(newTask))).andExpect(status().isUnauthorized());

        // Try to get tasks without token
        mockMvc.perform(get("/api/tasks")).andExpect(status().isUnauthorized());
    }

    @Test
    void shouldNotAllowAccessWithInvalidToken() throws Exception {
        // Try to get tasks with invalid token
        mockMvc.perform(get("/api/tasks").header("Authorization", "Bearer invalid-token")).andExpect(status().isUnauthorized());
    }

    @Test
    void shouldIsolateTasksBetweenUsers() throws Exception {
        // Create task with first user
        TaskDto task1 = new TaskDto("User1 Task", "Description", LocalDate.now().plusDays(7), "TO_DO");

        mockMvc.perform(post("/api/tasks").header("Authorization", "Bearer " + jwtToken).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(task1))).andExpect(status().isCreated());

        // Register second user
        AuthDto.RegisterRequest user2Request = new AuthDto.RegisterRequest("testuser2", "test2@example.com", "password123");

        MvcResult result = mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(user2Request))).andExpect(status().isOk()).andReturn();

        String response = result.getResponse().getContentAsString();
        AuthDto.AuthResponse user2AuthResponse = objectMapper.readValue(response, AuthDto.AuthResponse.class);
        String user2Token = user2AuthResponse.getToken();

        // Create task with second user
        TaskDto task2 = new TaskDto("User2 Task", "Description", LocalDate.now().plusDays(7), "TO_DO");

        mockMvc.perform(post("/api/tasks").header("Authorization", "Bearer " + user2Token).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(task2))).andExpect(status().isCreated());

        // First user should only see their task
        mockMvc.perform(get("/api/tasks").header("Authorization", "Bearer " + jwtToken)).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].title").value("User1 Task"));

        // Second user should only see their task
        mockMvc.perform(get("/api/tasks").header("Authorization", "Bearer " + user2Token)).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].title").value("User2 Task"));
    }

    @Test
    void shouldCompleteAuthenticationFlow() throws Exception {
        // Test login with existing user (created in setUp)
        AuthDto.LoginRequest loginRequest = new AuthDto.LoginRequest("testuser", "password123");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(loginRequest))).andExpect(status().isOk()).andExpect(jsonPath("$.token").exists()).andExpect(jsonPath("$.username").value("testuser")).andExpect(jsonPath("$.email").value("test@example.com")).andReturn();

        String loginResponse = loginResult.getResponse().getContentAsString();
        AuthDto.AuthResponse authResponse = objectMapper.readValue(loginResponse, AuthDto.AuthResponse.class);
        String loginToken = authResponse.getToken();

        // Use login token to access protected endpoint
        mockMvc.perform(get("/api/tasks").header("Authorization", "Bearer " + loginToken)).andExpect(status().isOk());
    }

    @Test
    void shouldRejectInvalidCredentials() throws Exception {
        AuthDto.LoginRequest invalidRequest = new AuthDto.LoginRequest("testuser", "wrongpassword");

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(invalidRequest))).andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").value("Invalid username or password"));
    }

    @Test
    void shouldRejectDuplicateRegistration() throws Exception {
        // Try to register with same username
        AuthDto.RegisterRequest duplicateRequest = new AuthDto.RegisterRequest("testuser", "different@example.com", "password123");

        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(duplicateRequest))).andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").value("Username already exists"));
    }
}