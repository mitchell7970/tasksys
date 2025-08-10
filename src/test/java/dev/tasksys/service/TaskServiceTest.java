package dev.tasksys.service;

import dev.tasksys.model.TaskDto;
import dev.tasksys.model.Task;
import dev.tasksys.model.TaskStatus;
import dev.tasksys.exception.TaskNotFoundException;
import dev.tasksys.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    private Task testTask;
    private TaskDto testTaskDto;

    @BeforeEach
    void setUp() {
        testTask = new Task("Test Task", "Test Description", LocalDate.now(), TaskStatus.TO_DO);
        testTask.setId(1L);

        testTaskDto = new TaskDto("Test Task", "Test Description", LocalDate.now(), "TO_DO");
        testTaskDto.setId(1L);
    }

    @Test
    void shouldCreateTask() {
        // Given
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        // When
        TaskDto result = taskService.createTask(testTaskDto);

        // Then
        assertThat(result.getTitle()).isEqualTo("Test Task");
        assertThat(result.getStatus()).isEqualTo("TO_DO");
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void shouldGetAllTasks() {
        // Given
        List<Task> tasks = Arrays.asList(testTask);
        when(taskRepository.findAll()).thenReturn(tasks);

        // When
        List<TaskDto> result = taskService.getAllTasks();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getTitle()).isEqualTo("Test Task");
        verify(taskRepository).findAll();
    }

    @Test
    void shouldGetTaskById() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

        // When
        TaskDto result = taskService.getTaskById(1L);

        // Then
        assertThat(result.getTitle()).isEqualTo("Test Task");
        assertThat(result.getId()).isEqualTo(1L);
        verify(taskRepository).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenTaskNotFound() {
        // Given
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> taskService.getTaskById(999L))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessage("Task not found with id: 999");
    }

    @Test
    void shouldUpdateTask() {
        // Given
        TaskDto updateDto = new TaskDto("Updated Task", "Updated Description",
                LocalDate.now().plusDays(1), "IN_PROGRESS");

        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        // When
        TaskDto result = taskService.updateTask(1L, updateDto);

        // Then
        verify(taskRepository).findById(1L);
        verify(taskRepository).save(any(Task.class));
        assertThat(result).isNotNull();
    }

    @Test
    void shouldDeleteTask() {
        // Given
        when(taskRepository.existsById(1L)).thenReturn(true);

        // When
        taskService.deleteTask(1L);

        // Then
        verify(taskRepository).existsById(1L);
        verify(taskRepository).deleteById(1L);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentTask() {
        // Given
        when(taskRepository.existsById(999L)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> taskService.deleteTask(999L))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessage("Task not found with id: 999");
    }

    @Test
    void shouldGetTasksByStatus() {
        // Given
        List<Task> tasks = Arrays.asList(testTask);
        when(taskRepository.findByStatus(TaskStatus.TO_DO)).thenReturn(tasks);

        // When
        List<TaskDto> result = taskService.getTasksByStatus("TO_DO");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getStatus()).isEqualTo("TO_DO");
        verify(taskRepository).findByStatus(TaskStatus.TO_DO);
    }
}