package dev.tasksys.repository;

import dev.tasksys.model.Task;
import dev.tasksys.model.TaskStatus;
import dev.tasksys.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class TaskRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TaskRepository taskRepository;

    private User user1;
    private User user2;
    private Task task1;
    private Task task2;
    private Task task3;

    @BeforeEach
    void setUp() {
        // Create test users
        user1 = new User("user1", "user1@example.com", "password");
        user2 = new User("user2", "user2@example.com", "password");

        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.flush();

        // Create test tasks
        task1 = new Task("Task 1", "Description 1", LocalDate.now(), TaskStatus.TO_DO, user1);
        task2 = new Task("Task 2", "Description 2", LocalDate.now(), TaskStatus.DONE, user1);
        task3 = new Task("Task 3", "Description 3", LocalDate.now(), TaskStatus.TO_DO, user2);

        entityManager.persist(task1);
        entityManager.persist(task2);
        entityManager.persist(task3);
        entityManager.flush();
    }

    @Test
    void shouldFindTasksByUserId() {
        // When
        List<Task> user1Tasks = taskRepository.findByUserId(user1.getId());
        List<Task> user2Tasks = taskRepository.findByUserId(user2.getId());

        // Then
        assertThat(user1Tasks).hasSize(2);
        assertThat(user2Tasks).hasSize(1);
        assertThat(user1Tasks).allMatch(task -> task.getUser().getId().equals(user1.getId()));
        assertThat(user2Tasks).allMatch(task -> task.getUser().getId().equals(user2.getId()));
    }

    @Test
    void shouldFindTasksByUserIdAndStatus() {
        // When
        List<Task> user1TodoTasks = taskRepository.findByUserIdAndStatus(user1.getId(), TaskStatus.TO_DO);
        List<Task> user1DoneTasks = taskRepository.findByUserIdAndStatus(user1.getId(), TaskStatus.DONE);
        List<Task> user2TodoTasks = taskRepository.findByUserIdAndStatus(user2.getId(), TaskStatus.TO_DO);

        // Then
        assertThat(user1TodoTasks).hasSize(1);
        assertThat(user1DoneTasks).hasSize(1);
        assertThat(user2TodoTasks).hasSize(1);

        assertThat(user1TodoTasks.getFirst().getStatus()).isEqualTo(TaskStatus.TO_DO);
        assertThat(user1DoneTasks.getFirst().getStatus()).isEqualTo(TaskStatus.DONE);
    }

    @Test
    void shouldFindTaskByIdAndUserId() {
        // When
        Optional<Task> foundTask = taskRepository.findByIdAndUserId(task1.getId(), user1.getId());
        Optional<Task> notFoundTask = taskRepository.findByIdAndUserId(task1.getId(), user2.getId());

        // Then
        assertThat(foundTask).isPresent();
        assertThat(foundTask.get().getTitle()).isEqualTo("Task 1");
        assertThat(notFoundTask).isEmpty();
    }

    @Test
    void shouldFindTasksByUserIdAndTitleContaining() {
        // When
        List<Task> foundTasks = taskRepository.findByUserIdAndTitleContainingIgnoreCase(user1.getId(), "task");
        List<Task> notFoundTasks = taskRepository.findByUserIdAndTitleContainingIgnoreCase(user2.getId(), "nonexistent");

        // Then
        assertThat(foundTasks).hasSize(2);
        assertThat(notFoundTasks).isEmpty();
        assertThat(foundTasks).allMatch(task -> task.getTitle().toLowerCase().contains("task") && task.getUser().getId().equals(user1.getId()));
    }

    @Test
    void shouldNotReturnTasksFromDifferentUser() {
        // When
        List<Task> user1Tasks = taskRepository.findByUserId(user1.getId());
        List<Task> user2Tasks = taskRepository.findByUserId(user2.getId());

        // Then
        assertThat(user1Tasks).noneMatch(task -> task.getUser().getId().equals(user2.getId()));
        assertThat(user2Tasks).noneMatch(task -> task.getUser().getId().equals(user1.getId()));
    }

    @Test
    void shouldDeleteTaskAndMaintainUserSeparation() {
        // Given
        Long taskToDeleteId = task1.getId();

        // When
        taskRepository.deleteById(taskToDeleteId);
        entityManager.flush();

        // Then
        List<Task> user1Tasks = taskRepository.findByUserId(user1.getId());
        List<Task> user2Tasks = taskRepository.findByUserId(user2.getId());

        assertThat(user1Tasks).hasSize(1); // Only task2 should remain for user1
        assertThat(user2Tasks).hasSize(1); // user2's task should be unaffected
        assertThat(user1Tasks.getFirst().getTitle()).isEqualTo("Task 2");
    }
}