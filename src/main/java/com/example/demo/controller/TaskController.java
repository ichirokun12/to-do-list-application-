package com.example.demo.controller;

import com.example.demo.entity.Task;
import com.example.demo.entity.User;
import com.example.demo.services.TaskServices;
import com.example.demo.services.UserServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/tasks")
@CrossOrigin(origins = "http://localhost:8082", allowCredentials = "true")
public class TaskController {

    @Autowired
    private TaskServices taskServices;

    @Autowired
    private UserServices userServices;

    @GetMapping("/{id}")
    public ResponseEntity<Optional<Task>> getTaskById(@PathVariable Long id) {
        try {
            Optional<Task> task = taskServices.getTaskById(id);
            if (task.isPresent()) {
                return new ResponseEntity<>(task, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Task>> getUserTasks(@PathVariable Long userId) {
        try {
            // Verify user exists
            Optional<User> user = userServices.findByUserId(userId);
            if (user.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            List<Task> tasks = taskServices.getTasksByUserId(userId);
            return new ResponseEntity<>(tasks, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/current")
    public ResponseEntity<List<Task>> getCurrentUserTasks() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated() ||
                    "anonymousUser".equals(authentication.getName())) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            String username = authentication.getName();
            Optional<User> user = userServices.findByUsername(username);

            if (user.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            List<Task> tasks = taskServices.getTasksByUserId(user.get().getUserId());
            return new ResponseEntity<>(tasks, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/addById/{userId}")
    public ResponseEntity<String> addTaskByUserId(@RequestBody Task task, @PathVariable Long userId) {
        try {
            Task savedTask = taskServices.addTasks(
                    task.getTask(),
                    task.getPriority(),
                    task.getStatus(),
                    task.getStartTime(),
                    task.getEndTime(),
                    task.getStartDate(),
                    task.getEndDate(),
                    userId
            );
            return new ResponseEntity<>("Task created successfully with ID: " + savedTask.getTaskId(), HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Failed to create task: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("Internal server error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/add")
    public ResponseEntity<?> addTaskForCurrentUser(@RequestBody Task task) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated() ||
                    "anonymousUser".equals(authentication.getName())) {
                return new ResponseEntity<>("User not authenticated", HttpStatus.UNAUTHORIZED);
            }

            String username = authentication.getName();
            Optional<User> user = userServices.findByUsername(username);

            if (user.isEmpty()) {
                return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
            }

            Task savedTask = taskServices.addTasks(
                    task.getTask(),
                    task.getPriority(),
                    task.getStatus(),
                    task.getStartTime(),
                    task.getEndTime(),
                    task.getStartDate(),
                    task.getEndDate(),
                    user.get().getUserId()
            );

            return new ResponseEntity<>("Task created successfully with ID: " + savedTask.getTaskId(), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to create task: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ADDED: Form-based mark done endpoint for Thymeleaf
    @PostMapping("/markDone/{taskId}")
    public String markTaskDoneForm(@PathVariable Long taskId, RedirectAttributes redirectAttributes) {
        try {
            taskServices.markTaskDone(taskId);
            redirectAttributes.addFlashAttribute("success", "Task marked as done successfully!");
            return "redirect:/tasks";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update task: " + e.getMessage());
            return "redirect:/tasks";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Internal server error: " + e.getMessage());
            return "redirect:/tasks";
        }
    }

    // ADDED: REST API version for mark done
    @PutMapping("/markDone/{taskId}")
    public ResponseEntity<String> markTaskDone(@PathVariable Long taskId) {
        try {
            taskServices.markTaskDone(taskId);
            return new ResponseEntity<>("Task marked as done successfully", HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Failed to update task: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("Internal server error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/update/{taskId}")
    public ResponseEntity<String> updateTask(@PathVariable Long taskId, @RequestBody Task updatedTask) {
        try {
            Optional<Task> existingTask = taskServices.getTaskById(taskId);
            if (existingTask.isEmpty()) {
                return new ResponseEntity<>("Task not found", HttpStatus.NOT_FOUND);
            }

            Task task = existingTask.get();

            // Update fields if provided
            if (updatedTask.getTask() != null) {
                task.setTask(updatedTask.getTask());
            }
            if (updatedTask.getStatus() != null) {
                task.setStatus(updatedTask.getStatus());
            }
            if (updatedTask.getPriority() != null) {
                task.setPriority(updatedTask.getPriority());
            }
            if (updatedTask.getStartTime() != null) {
                task.setStartTime(updatedTask.getStartTime());
            }
            if (updatedTask.getEndTime() != null) {
                task.setEndTime(updatedTask.getEndTime());
            }
            if (updatedTask.getStartDate() != null) {
                task.setStartDate(updatedTask.getStartDate());
            }
            if (updatedTask.getEndDate() != null) {
                task.setEndDate(updatedTask.getEndDate());
            }

            taskServices.updateTask(task);
            return new ResponseEntity<>("Task updated successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to update task: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ADDED: Form-based delete endpoint for Thymeleaf
    @PostMapping("/delete/{taskId}")
    public String deleteTaskForm(@PathVariable Long taskId, RedirectAttributes redirectAttributes) {
        try {
            // Check if task exists
            Optional<Task> task = taskServices.getTaskById(taskId);
            if (task.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Task not found");
                return "redirect:/tasks";
            }

            taskServices.deleteTask(taskId);
            redirectAttributes.addFlashAttribute("success", "Task deleted successfully!");
            return "redirect:/tasks";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete task: " + e.getMessage());
            return "redirect:/tasks";
        }
    }

    // ADDED: REST API version for delete
    @DeleteMapping("/delete/{taskId}")
    public ResponseEntity<String> deleteTask(@PathVariable Long taskId) {
        try {
            // Check if task exists
            Optional<Task> task = taskServices.getTaskById(taskId);
            if (task.isEmpty()) {
                return new ResponseEntity<>("Task not found", HttpStatus.NOT_FOUND);
            }

            taskServices.deleteTask(taskId);
            return new ResponseEntity<>("Task deleted successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to delete task: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}