package dev.tasksys.service;

import dev.tasksys.model.TaskDto;
import dev.tasksys.model.Task;
import dev.tasksys.model.TaskStatus;
import dev.tasksys.model.User;
import dev.tasksys.exception.TaskNotFoundException;
import dev.tasksys.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }

    public TaskDto createTask(TaskDto taskDto) {
        User currentUser = getCurrentUser();
        Task task = convertToEntity(taskDto);
        task.setUser(currentUser);
        Task savedTask = taskRepository.save(task);
        return convertToDto(savedTask);
    }

    public List<TaskDto> getAllTasks() {
        User currentUser = getCurrentUser();
        return taskRepository.findByUserId(currentUser.getId()).stream()
                .map(this::convertToDto)
                .toList();
    }

    public TaskDto getTaskById(Long id) {
        User currentUser = getCurrentUser();
        Task task = taskRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + id));
        return convertToDto(task);
    }

    public TaskDto updateTask(Long id, TaskDto taskDto) {
        User currentUser = getCurrentUser();
        Task existingTask = taskRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + id));

        existingTask.setTitle(taskDto.getTitle());
        existingTask.setDescription(taskDto.getDescription());
        existingTask.setDueDate(taskDto.getDueDate());
        existingTask.setStatus(TaskStatus.valueOf(taskDto.getStatus()));

        Task updatedTask = taskRepository.save(existingTask);
        return convertToDto(updatedTask);
    }

    public void deleteTask(Long id) {
        User currentUser = getCurrentUser();
        Task task = taskRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + id));
        taskRepository.delete(task);
    }

    public List<TaskDto> getTasksByStatus(String status) {
        User currentUser = getCurrentUser();
        TaskStatus taskStatus = TaskStatus.valueOf(status);
        return taskRepository.findByUserIdAndStatus(currentUser.getId(), taskStatus).stream()
                .map(this::convertToDto)
                .toList();
    }

    private TaskDto convertToDto(Task task) {
        TaskDto dto = new TaskDto();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setDueDate(task.getDueDate());
        dto.setStatus(task.getStatus().name());
        return dto;
    }

    private Task convertToEntity(TaskDto dto) {
        Task task = new Task();
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setDueDate(dto.getDueDate());
        task.setStatus(TaskStatus.valueOf(dto.getStatus()));
        return task;
    }
}