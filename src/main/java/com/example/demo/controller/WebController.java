package com.example.demo.controller;

import com.example.demo.entity.Task;
import com.example.demo.entity.User;
import com.example.demo.services.TaskServices;
import com.example.demo.services.UserServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;import com.example.demo.entity.User;
import com.example.demo.services.UserServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class WebController {

    @Autowired
    private TaskServices taskServices;

    @Autowired
    private UserServices userServices;

    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            @RequestParam(value = "registered", required = false) String registered,
            Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid username or password.");
        }
        if (logout != null) {
            model.addAttribute("logout", "You have been logged out successfully.");
        }
        if (registered != null) {
            model.addAttribute("success", "Registration successful! Please sign in with your credentials.");
        }
        return "login";
    }

    // Add this to handle /register and redirect to /auth/register
    @GetMapping("/register")
    public String showRegisterFormRedirect() {
        return "redirect:/auth/register";
    }

    @GetMapping("/auth/register")
    public String showRegisterForm(
            @RequestParam(value = "error", required = false) String error,
            Model model) {
        if (error != null) {
            model.addAttribute("error", "Registration failed. Please try again.");
        }
        return "register";
    }

    @PostMapping("/auth/register")
    public String processRegistration(
            @ModelAttribute User user,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            if (!user.getPassword().equals(user.getPassword())) {
                model.addAttribute("error", "Passwords do not match.");
                return "register";
            }
            if (userServices.existsByUsername(user.getUsername())) {
                model.addAttribute("error", "Username already exists.");
                return "register";
            }
            if (userServices.existsByEmail(user.getEmail())) {
                model.addAttribute("error", "Email already exists.");
                return "register";
            }
            userServices.createUser(
                    user.getFirstName(),
                    user.getLastName(),
                    user.getUsername(),
                    user.getPassword(),
                    user.getEmail()
            );
            redirectAttributes.addAttribute("registered", true);
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("error", "Registration failed: " + e.getMessage());
            return "register";
        }
    }
    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }

    // FIXED: Actually fetch tasks from database
    @GetMapping("/tasks")
    public String tasks(Model model) {
        List<Task> tasks = new ArrayList<>();
        String currentUsername = "Not logged in";

        try {
            // Get current user's tasks
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.isAuthenticated() &&
                    !"anonymousUser".equals(authentication.getName())) {

                currentUsername = authentication.getName();
                System.out.println("Loading tasks for user: " + currentUsername);

                Optional<User> user = userServices.findByUsername(currentUsername);

                if (user.isPresent()) {
                    Long userId = user.get().getUserId();
                    System.out.println("User ID: " + userId);

                    // ACTUALLY fetch tasks from database
                    tasks = taskServices.getTasksByUserId(userId);
                    System.out.println("Found " + tasks.size() + " tasks for user: " + currentUsername);

                    // Print each task for debugging
                    for (Task task : tasks) {
                        System.out.println("Task: " + task.getTask() +
                                ", ID: " + task.getTaskId() +
                                ", Status: " + task.getStatus() +
                                ", Priority: " + task.getPriority());
                    }
                } else {
                    System.out.println("User not found: " + currentUsername);
                }
            } else {
                System.out.println("User not authenticated");
            }

        } catch (Exception e) {
            System.out.println("Error loading tasks for user " + currentUsername + ": " + e.getMessage());
            e.printStackTrace();
            tasks = new ArrayList<>();
        }

        // Add debug info to model
        model.addAttribute("tasks", tasks);
        model.addAttribute("tasksCount", tasks.size());
        model.addAttribute("currentUser", currentUsername);

        System.out.println("Sending " + tasks.size() + " tasks to template");
        return "tasks";
    }

    // ADD: Debug endpoint to check what's in the database
    @GetMapping("/tasks/debug")
    @ResponseBody
    public String debugTasks() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication != null ? authentication.getName() : "anonymous";

            List<Task> allTasks = taskServices.getAllTasks();
            StringBuilder sb = new StringBuilder();

            sb.append("=== TASK DEBUG INFO ===\n");
            sb.append("Current user: ").append(username).append("\n");
            sb.append("Total tasks in database: ").append(allTasks.size()).append("\n\n");

            if (allTasks.isEmpty()) {
                sb.append("No tasks found in database!\n");
            } else {
                for (Task task : allTasks) {
                    sb.append("Task ID: ").append(task.getTaskId())
                            .append(", Title: ").append(task.getTask())
                            .append(", Status: ").append(task.getStatus())
                            .append(", Priority: ").append(task.getPriority())
                            .append(", User: ").append(
                                    task.getAssignedUser() != null ?
                                            task.getAssignedUser().getUsername() : "null"
                            ).append("\n");
                }
            }

            // Also check current user's tasks specifically
            if (!"anonymous".equals(username)) {
                Optional<User> user = userServices.findByUsername(username);
                if (user.isPresent()) {
                    List<Task> userTasks = taskServices.getTasksByUserId(user.get().getUserId());
                    sb.append("\n=== CURRENT USER TASKS ===\n");
                    sb.append("Tasks for user ").append(username).append(": ").append(userTasks.size()).append("\n");
                }
            }

            return sb.toString().replace("\n", "<br>");

        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // Form submission handler for adding tasks
    @PostMapping("/tasks/form/add")
    public String addTask(
            @RequestParam("task") String taskTitle,
            @RequestParam("priority") String priority,
            @RequestParam("status") String status,
            @RequestParam(value = "startTime", required = false) String startTime,
            @RequestParam(value = "endTime", required = false) String endTime,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            System.out.println("=== FORM SUBMISSION ===");
            System.out.println("Task: " + taskTitle);
            System.out.println("Priority: " + priority);
            System.out.println("Status: " + status);

            if (authentication == null || !authentication.isAuthenticated() ||
                    "anonymousUser".equals(authentication.getName())) {
                redirectAttributes.addFlashAttribute("error", "User not authenticated");
                return "redirect:/login";
            }

            String username = authentication.getName();
            Optional<User> user = userServices.findByUsername(username);

            if (user.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "User not found");
                return "redirect:/tasks";
            }

            // Create the task
            Task savedTask = taskServices.addTasks(
                    taskTitle,
                    priority,
                    status,
                    startTime,
                    endTime,
                    startDate,
                    endDate,
                    user.get().getUserId()
            );

            System.out.println("Task created successfully with ID: " + savedTask.getTaskId());
            redirectAttributes.addFlashAttribute("success", "Task added successfully!");

        } catch (Exception e) {
            System.out.println("Error adding task: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to add task: " + e.getMessage());
        }

        return "redirect:/tasks";
    }
}