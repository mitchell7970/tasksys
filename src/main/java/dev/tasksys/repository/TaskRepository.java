package dev.tasksys.repository;

import dev.tasksys.model.Task;
import dev.tasksys.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByUserIdAndStatus(Long userId, TaskStatus status);
    List<Task> findByUserId(Long userId);
    Optional<Task> findByIdAndUserId(Long id, Long userId);
    List<Task> findByUserIdAndTitleContainingIgnoreCase(Long userId, String title);
}