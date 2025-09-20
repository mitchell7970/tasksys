package dev.tasksys.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.tasksys.config.JwtUtil;
import dev.tasksys.model.TaskDto;
import dev.tasksys.model.User;
import dev.tasksys.exception.TaskNotFoundException;
import dev.tasksys.service.TaskService;
import dev.tasksys.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
@Import({JwtUtil.class})
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private TaskDto testTaskDto;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "test@example.com", "password");
        testUser.setId(1L);

        testTaskDto = new TaskDto("Test Task", "Test Description", LocalDate.now(), "TO_DO");
        testTaskDto.setId(1L);
    }

    @Test
    void shouldReturn401ForUnauthenticatedRequest() throws Exception {
        mockMvc.perform(get("/api/tasks")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldCreateTaskWhenAuthenticated() throws Exception {
        // Given
        when(taskService.createTask(any(TaskDto.class))).thenReturn(testTaskDto);

        // When & Then
        mockMvc.perform(post("/api/tasks").with(jwt()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(testTaskDto))).andExpect(status().isCreated()).andExpect(jsonPath("$.title").value("Test Task")).andExpect(jsonPath("$.status").value("TO_DO"));

        verify(taskService).createTask(any(TaskDto.class));
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldGetAllTasksWhenAuthenticated() throws Exception {
        // Given
        List<TaskDto> tasks = Arrays.asList(testTaskDto);
        when(taskService.getAllTasks()).thenReturn(tasks);

        // When & Then
        mockMvc.perform(get("/api/tasks").with(jwt())).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1))).andExpect(jsonPath("$[0].title").value("Test Task"));

        verify(taskService).getAllTasks();
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldGetTaskByIdWhenAuthenticated() throws Exception {
        // Given
        when(taskService.getTaskById(1L)).thenReturn(testTaskDto);

        // When & Then
        mockMvc.perform(get("/api/tasks/1").with(jwt())).andExpect(status().isOk()).andExpect(jsonPath("$.title").value("Test Task")).andExpect(jsonPath("$.id").value(1));

        verify(taskService).getTaskById(1L);
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldReturnNotFoundWhenTaskNotExistsForUser() throws Exception {
        // Given
        when(taskService.getTaskById(999L)).thenThrow(new TaskNotFoundException("Task not found"));

        // When & Then
        mockMvc.perform(get("/api/tasks/999").with(jwt())).andExpect(status().isNotFound()).andExpect(jsonPath("$.error").value("Task not found"));

        verify(taskService).getTaskById(999L);
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldUpdateTaskWhenAuthenticated() throws Exception {
        // Given
        TaskDto updatedTask = new TaskDto("Updated Task", "Updated Description", LocalDate.now(), "IN_PROGRESS");
        updatedTask.setId(1L);

        when(taskService.updateTask(eq(1L), any(TaskDto.class))).thenReturn(updatedTask);

        // When & Then
        mockMvc.perform(put("/api/tasks/1").with(jwt()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(updatedTask))).andExpect(status().isOk()).andExpect(jsonPath("$.title").value("Updated Task")).andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        verify(taskService).updateTask(eq(1L), any(TaskDto.class));
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldDeleteTaskWhenAuthenticated() throws Exception {
        // Given
        doNothing().when(taskService).deleteTask(1L);

        // When & Then
        mockMvc.perform(delete("/api/tasks/1").with(jwt())).andExpect(status().isNoContent());

        verify(taskService).deleteTask(1L);
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldGetTasksByStatusWhenAuthenticated() throws Exception {
        // Given
        List<TaskDto> tasks = Arrays.asList(testTaskDto);
        when(taskService.getTasksByStatus("TO_DO")).thenReturn(tasks);

        // When & Then
        mockMvc.perform(get("/api/tasks/status/TO_DO").with(jwt())).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1))).andExpect(jsonPath("$[0].status").value("TO_DO"));

        verify(taskService).getTasksByStatus("TO_DO");
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldReturnBadRequestForInvalidTask() throws Exception {
        // Given
        TaskDto invalidTask = new TaskDto("", "", null, "");

        // When & Then
        mockMvc.perform(post("/api/tasks").with(jwt()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(invalidTask))).andExpect(status().isBadRequest()).andExpect(jsonPath("$.title").exists()).andExpect(jsonPath("$.dueDate").exists()).andExpect(jsonPath("$.status").exists());
    }

    @Test
    void shouldReturn401ForInvalidJwtToken() throws Exception {
        mockMvc.perform(get("/api/tasks").header("Authorization", "Bearer invalid-token")).andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401ForMissingAuthorizationHeader() throws Exception {
        mockMvc.perform(get("/api/tasks")).andExpect(status().isUnauthorized());
    }
}