package dev.tasksys.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.tasksys.model.TaskDto;
import dev.tasksys.exception.TaskNotFoundException;
import dev.tasksys.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @Autowired
    private ObjectMapper objectMapper;

    private TaskDto testTaskDto;

    @BeforeEach
    void setUp() {
        testTaskDto = new TaskDto("Test Task", "Test Description", LocalDate.now(), "TO_DO");
        testTaskDto.setId(1L);
    }

    @Test
    void shouldCreateTask() throws Exception {
        // Given
        when(taskService.createTask(any(TaskDto.class))).thenReturn(testTaskDto);

        // When & Then
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testTaskDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test Task"))
                .andExpect(jsonPath("$.status").value("TO_DO"));

        verify(taskService).createTask(any(TaskDto.class));
    }

    @Test
    void shouldGetAllTasks() throws Exception {
        // Given
        List<TaskDto> tasks = Arrays.asList(testTaskDto);
        when(taskService.getAllTasks()).thenReturn(tasks);

        // When & Then
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Test Task"));

        verify(taskService).getAllTasks();
    }

    @Test
    void shouldGetTaskById() throws Exception {
        // Given
        when(taskService.getTaskById(1L)).thenReturn(testTaskDto);

        // When & Then
        mockMvc.perform(get("/api/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Task"))
                .andExpect(jsonPath("$.id").value(1));

        verify(taskService).getTaskById(1L);
    }

    @Test
    void shouldReturnNotFoundWhenTaskNotExists() throws Exception {
        // Given
        when(taskService.getTaskById(999L)).thenThrow(new TaskNotFoundException("Task not found"));

        // When & Then
        mockMvc.perform(get("/api/tasks/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Task not found"));

        verify(taskService).getTaskById(999L);
    }

    @Test
    void shouldUpdateTask() throws Exception {
        // Given
        TaskDto updatedTask = new TaskDto("Updated Task", "Updated Description",
                LocalDate.now(), "IN_PROGRESS");
        updatedTask.setId(1L);

        when(taskService.updateTask(eq(1L), any(TaskDto.class))).thenReturn(updatedTask);

        // When & Then
        mockMvc.perform(put("/api/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedTask)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Task"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        verify(taskService).updateTask(eq(1L), any(TaskDto.class));
    }

    @Test
    void shouldDeleteTask() throws Exception {
        // Given
        doNothing().when(taskService).deleteTask(1L);

        // When & Then
        mockMvc.perform(delete("/api/tasks/1"))
                .andExpect(status().isNoContent());

        verify(taskService).deleteTask(1L);
    }

    @Test
    void shouldGetTasksByStatus() throws Exception {
        // Given
        List<TaskDto> tasks = Arrays.asList(testTaskDto);
        when(taskService.getTasksByStatus("TO_DO")).thenReturn(tasks);

        // When & Then
        mockMvc.perform(get("/api/tasks/status/TO_DO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status").value("TO_DO"));

        verify(taskService).getTasksByStatus("TO_DO");
    }

    @Test
    void shouldReturnBadRequestForInvalidTask() throws Exception {
        // Given
        TaskDto invalidTask = new TaskDto("", "", null, "");

        // When & Then
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidTask)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.dueDate").exists())
                .andExpect(jsonPath("$.status").exists());
    }
}