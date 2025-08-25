package com.example.demo.controller;


import com.example.demo.entity.Task;
import com.example.demo.services.TaskServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Controller
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskServices taskServices;

    @GetMapping
    public String getTasks(Model model) {
        List<Task> tasks = taskServices.getAllTasks();
        model.addAttribute("tasks", tasks);
        return "tasks";
    }


    @PostMapping
    public String addAllTasks(@RequestParam String task,
                              @RequestParam String priority,
                              @RequestParam String status,
                              @RequestParam(required = false) String startTime,
                              @RequestParam(required = false) String endTime,
                              @RequestParam(required = false) String startDate,
                              @RequestParam(required = false) String endDate) {
        taskServices.addTasks(task, priority, status, startTime, endTime, startDate, endDate);
        return "redirect:/tasks?added=true";
    }
    @PostMapping("/delete/{id}")
    public String deleteTask(@PathVariable Long id) {
        taskServices.deleteTask(id);
        return "redirect:/tasks";
    }

    @PostMapping("/markDone/{id}")
    public String markTaskDone(@PathVariable Long id) {
        taskServices.markTaskDone(id);
        return "redirect:/tasks";
    }
}
