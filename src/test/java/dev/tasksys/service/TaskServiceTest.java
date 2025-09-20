package dev.tasksys.service;

import dev.tasksys.model.TaskDto;
import dev.tasksys.model.Task;
import dev.tasksys.model.TaskStatus;
import dev.tasksys.model.User;
import dev.tasksys.exception.TaskNotFoundException;
import dev.tasksys.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private TaskService taskService;

    private Task testTask;
    private TaskDto testTaskDto;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "test@example.com", "password");
        testUser.setId(1L);

        testTask = new Task("Test Task", "Test Description", LocalDate.now(), TaskStatus.TO_DO, testUser);
        testTask.setId(1L);

        testTaskDto = new TaskDto("Test Task", "Test Description", LocalDate.now(), "TO_DO");
        testTaskDto.setId(1L);
    }

    @Test
    void shouldCreateTask() {
        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            // Given
            mockedSecurityContext.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(testUser);
            when(taskRepository.save(any(Task.class))).thenReturn(testTask);

            // When
            TaskDto result = taskService.createTask(testTaskDto);

            // Then
            assertThat(result.getTitle()).isEqualTo("Test Task");
            assertThat(result.getStatus()).isEqualTo("TO_DO");
            verify(taskRepository).save(any(Task.class));
        }
    }

    @Test
    void shouldGetAllTasksForCurrentUser() {
        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            // Given
            mockedSecurityContext.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(testUser);

            List<Task> tasks = Arrays.asList(testTask);
            when(taskRepository.findByUserId(1L)).thenReturn(tasks);

            // When
            List<TaskDto> result = taskService.getAllTasks();

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getTitle()).isEqualTo("Test Task");
            verify(taskRepository).findByUserId(1L);
        }
    }

    @Test
    void shouldGetTaskByIdForCurrentUser() {
        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            // Given
            mockedSecurityContext.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(testUser);
            when(taskRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testTask));

            // When
            TaskDto result = taskService.getTaskById(1L);

            // Then
            assertThat(result.getTitle()).isEqualTo("Test Task");
            assertThat(result.getId()).isEqualTo(1L);
            verify(taskRepository).findByIdAndUserId(1L, 1L);
        }
    }

    @Test
    void shouldThrowExceptionWhenTaskNotFoundForUser() {
        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            // Given
            mockedSecurityContext.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(testUser);
            when(taskRepository.findByIdAndUserId(999L, 1L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> taskService.getTaskById(999L)).isInstanceOf(TaskNotFoundException.class).hasMessage("Task not found with id: 999");
        }
    }

    @Test
    void shouldUpdateTaskForCurrentUser() {
        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            // Given
            mockedSecurityContext.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(testUser);

            TaskDto updateDto = new TaskDto("Updated Task", "Updated Description", LocalDate.now().plusDays(1), "IN_PROGRESS");

            when(taskRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testTask));
            when(taskRepository.save(any(Task.class))).thenReturn(testTask);

            // When
            TaskDto result = taskService.updateTask(1L, updateDto);

            // Then
            verify(taskRepository).findByIdAndUserId(1L, 1L);
            verify(taskRepository).save(any(Task.class));
            assertThat(result).isNotNull();
        }
    }

    @Test
    void shouldDeleteTaskForCurrentUser() {
        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            // Given
            mockedSecurityContext.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(testUser);
            when(taskRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testTask));

            // When
            taskService.deleteTask(1L);

            // Then
            verify(taskRepository).findByIdAndUserId(1L, 1L);
            verify(taskRepository).delete(testTask);
        }
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentTaskForUser() {
        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            // Given
            mockedSecurityContext.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(testUser);
            when(taskRepository.findByIdAndUserId(999L, 1L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> taskService.deleteTask(999L)).isInstanceOf(TaskNotFoundException.class).hasMessage("Task not found with id: 999");
        }
    }

    @Test
    void shouldGetTasksByStatusForCurrentUser() {
        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            // Given
            mockedSecurityContext.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(testUser);

            List<Task> tasks = Arrays.asList(testTask);
            when(taskRepository.findByUserIdAndStatus(1L, TaskStatus.TO_DO)).thenReturn(tasks);

            // When
            List<TaskDto> result = taskService.getTasksByStatus("TO_DO");

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getStatus()).isEqualTo("TO_DO");
            verify(taskRepository).findByUserIdAndStatus(1L, TaskStatus.TO_DO);
        }
    }

    @Test
    void shouldNotAllowAccessToOtherUsersTask() {
        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            // Given
            User otherUser = new User("otheruser", "other@example.com", "password");
            otherUser.setId(2L);

            mockedSecurityContext.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(testUser);
            when(taskRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty()); // Task belongs to different user

            // When & Then
            assertThatThrownBy(() -> taskService.getTaskById(1L)).isInstanceOf(TaskNotFoundException.class).hasMessage("Task not found with id: 1");

            verify(taskRepository).findByIdAndUserId(1L, 1L); // Should query with current user's ID
        }
    }
}