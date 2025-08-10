package dev.tasksys.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.tasksys.TasksysApplication;
import dev.tasksys.model.TaskDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;

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

    @Test
    void shouldPerformFullTaskCRUDOperations() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Create task
        TaskDto newTask = new TaskDto("Integration Task", "Integration Description",
                LocalDate.now().plusDays(7), "TO_DO");

        String taskJson = objectMapper.writeValueAsString(newTask);

        String response = mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Integration Task"))
                .andReturn().getResponse().getContentAsString();

        TaskDto createdTask = objectMapper.readValue(response, TaskDto.class);
        Long taskId = createdTask.getId();

        // Get all tasks
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Integration Task"));

        // Get task by ID
        mockMvc.perform(get("/api/tasks/" + taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Integration Task"));

        // Update task
        TaskDto updateTask = new TaskDto("Updated Integration Task", "Updated Description",
                LocalDate.now().plusDays(10), "IN_PROGRESS");

        mockMvc.perform(put("/api/tasks/" + taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateTask)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Integration Task"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        // Get tasks by status
        mockMvc.perform(get("/api/tasks/status/IN_PROGRESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("IN_PROGRESS"));

        // Delete task
        mockMvc.perform(delete("/api/tasks/" + taskId))
                .andExpect(status().isNoContent());

        // Verify task is deleted
        mockMvc.perform(get("/api/tasks/" + taskId))
                .andExpect(status().isNotFound());
    }
}