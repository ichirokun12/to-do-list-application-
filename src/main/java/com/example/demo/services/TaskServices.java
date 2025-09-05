package com.example.demo.services;

import com.example.demo.entity.Task;
import com.example.demo.entity.User;
import com.example.demo.repository.TaskRepo;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TaskServices {

    @Autowired
    private TaskRepo taskRepo;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserServices userServices;

    public Optional<Task> getTaskById(Long taskId) {
        return taskRepo.findById(taskId);
    }

    public List<Task> getTasksByUserId(Long userId) {
        try {
            List<Task> tasks = taskRepo.findByAssignedUser_UserId(userId);
            System.out.println("Found " + tasks.size() + " tasks for user ID: " + userId);
            return tasks;
        } catch (Exception e) {
            System.out.println("Error fetching tasks for user " + userId + ": " + e.getMessage());
            e.printStackTrace();
            return List.of(); // Return empty list instead of null
        }
    }

    public List<Task> getAllTasks() {
        try {
            List<Task> allTasks = taskRepo.findAll();
            System.out.println("Total tasks in database: " + allTasks.size());
            return allTasks;
        } catch (Exception e) {
            System.out.println("Error fetching all tasks: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    public Task addTasks(String task, String priority, String status,
                         String startTime, String endTime, String startDate,
                         String endDate, Long userId) {

        try {
            System.out.println("=== ADDING NEW TASK ===");
            System.out.println("Task: " + task);
            System.out.println("Priority: " + priority);
            System.out.println("Status: " + status);
            System.out.println("User ID: " + userId);

            // Validate required fields
            if (task == null || task.trim().isEmpty()) {
                throw new RuntimeException("Task title cannot be empty");
            }

            // Find the user if userId is provided
            User assignedUser = null;
            if (userId != null) {
                Optional<User> userOptional = userRepository.findById(userId);
                if (userOptional.isPresent()) {
                    assignedUser = userOptional.get();
                    System.out.println("Found user: " + assignedUser.getUsername());
                } else {
                    throw new RuntimeException("User not found with ID: " + userId);
                }
            }

            Task newTask = Task.builder()
                    .task(task.trim())
                    .priority(priority != null && !priority.trim().isEmpty() ? priority.trim() : "medium")
                    .status(status != null && !status.trim().isEmpty() ? status.trim() : "not done")
                    .assignedUser(assignedUser)
                    .build();

            // Set optional fields only if they are not null and not empty
            if (startTime != null && !startTime.trim().isEmpty()) {
                newTask.setStartTime(startTime.trim());
            }

            if (endTime != null && !endTime.trim().isEmpty()) {
                newTask.setEndTime(endTime.trim());
            }

            if (startDate != null && !startDate.trim().isEmpty()) {
                newTask.setStartDate(startDate.trim());
            }

            if (endDate != null && !endDate.trim().isEmpty()) {
                newTask.setEndDate(endDate.trim());
            }

            System.out.println("Saving new Task entity: " + newTask);
            Task saved = taskRepo.save(newTask);
            System.out.println("Successfully saved Task with ID: " + saved.getTaskId());

            return saved;

        } catch (Exception e) {
            System.out.println("ERROR: Failed to save task - " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to create task: " + e.getMessage());
        }
    }

    public void deleteTask(Long taskId) {
        try {
            if (!taskRepo.existsById(taskId)) {
                throw new RuntimeException("Task not found with ID: " + taskId);
            }

            taskRepo.deleteById(taskId);
            System.out.println("Successfully deleted task with ID: " + taskId);

        } catch (Exception e) {
            System.out.println("Error deleting task " + taskId + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to delete task: " + e.getMessage());
        }
    }

    public void markTaskDone(Long taskId) {
        try {
            Optional<Task> taskOptional = taskRepo.findById(taskId);
            if (taskOptional.isPresent()) {
                Task task = taskOptional.get();
                task.setStatus("done");
                taskRepo.save(task);
                System.out.println("Successfully marked task " + taskId + " as done");
            } else {
                throw new RuntimeException("Task not found with ID: " + taskId);
            }
        } catch (Exception e) {
            System.out.println("Error marking task " + taskId + " as done: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to update task: " + e.getMessage());
        }
    }

    public Task updateTask(Task task) {
        try {
            if (!taskRepo.existsById(task.getTaskId())) {
                throw new RuntimeException("Task not found with ID: " + task.getTaskId());
            }

            Task updated = taskRepo.save(task);
            System.out.println("Successfully updated task with ID: " + updated.getTaskId());
            return updated;

        } catch (Exception e) {
            System.out.println("Error updating task: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to update task: " + e.getMessage());
        }
    }

    public List<Task> getTasksByStatus(String status) {
        return taskRepo.findByStatus(status);
    }

    public List<Task> getTasksByPriority(String priority) {
        return taskRepo.findByPriority(priority);
    }

    public List<Task> getTasksByUserIdAndStatus(Long userId, String status) {
        return taskRepo.findByAssignedUser_UserIdAndStatus(userId, status);
    }

    public Long getTaskCountByUserId(Long userId) {
        return taskRepo.countByAssignedUser_UserId(userId);
    }

    public Long getCompletedTaskCountByUserId(Long userId) {
        return taskRepo.countByAssignedUser_UserIdAndStatus(userId, "done");
    }
}