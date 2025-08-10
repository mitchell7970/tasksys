package dev.tasksys.repository;

import dev.tasksys.model.Task;
import dev.tasksys.model.TaskStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Test
    void shouldSaveAndFindTask() {
        // Given
        Task task = new Task("Test Task", "Description", LocalDate.now(), TaskStatus.TO_DO);

        // When
        Task saved = taskRepository.save(task);
        Optional<Task> found = taskRepository.findById(saved.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Test Task");
    }

    @Test
    void shouldFindTasksByStatus() {
        // Given
        Task task1 = new Task("Task 1", "Desc 1", LocalDate.now(), TaskStatus.TO_DO);
        Task task2 = new Task("Task 2", "Desc 2", LocalDate.now(), TaskStatus.DONE);
        Task task3 = new Task("Task 3", "Desc 3", LocalDate.now(), TaskStatus.TO_DO);

        taskRepository.saveAll(List.of(task1, task2, task3));

        // When
        List<Task> todoTasks = taskRepository.findByStatus(TaskStatus.TO_DO);

        // Then
        assertThat(todoTasks).hasSize(2);
        assertThat(todoTasks).allMatch(task -> task.getStatus() == TaskStatus.TO_DO);
    }

    @Test
    void shouldFindTasksByTitleContaining() {
        // Given
        Task task1 = new Task("Important Task", "Desc", LocalDate.now(), TaskStatus.TO_DO);
        Task task2 = new Task("Regular Work", "Desc", LocalDate.now(), TaskStatus.TO_DO);
        Task task3 = new Task("Important Meeting", "Desc", LocalDate.now(), TaskStatus.TO_DO);

        taskRepository.saveAll(List.of(task1, task2, task3));

        // When
        List<Task> importantTasks = taskRepository.findByTitleContainingIgnoreCase("important");

        // Then
        assertThat(importantTasks).hasSize(2);
        assertThat(importantTasks).allMatch(task ->
                task.getTitle().toLowerCase().contains("important"));
    }

    @Test
    void shouldDeleteTask() {
        // Given
        Task task = new Task("Delete Me", "Description", LocalDate.now(), TaskStatus.TO_DO);
        Task saved = taskRepository.save(task);

        // When
        taskRepository.deleteById(saved.getId());
        Optional<Task> found = taskRepository.findById(saved.getId());

        // Then
        assertThat(found).isEmpty();
    }
}