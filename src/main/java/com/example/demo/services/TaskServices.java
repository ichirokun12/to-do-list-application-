package com.example.demo.services;

import com.example.demo.entity.Task;
import com.example.demo.repository.TaskRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TaskServices {

    @Autowired
    private TaskRepo taskRepo;

    public List<Task> getAllTasks() {
        return taskRepo.findAll();
    }


    public Task addTasks(String task, String priority, String  status,  String startTime, String endTime, String startDate, String endDate) {
        Task tasks = new Task();
        tasks.setTask(task);
        tasks.setPriority(priority);
        tasks.setStatus(status);

        if (startTime != null && !startTime.trim().isEmpty()) {
            tasks.setStartTime(startTime.trim());
        }
        if (endTime != null && !endTime.trim().isEmpty()) {
            tasks.setEndTime(endTime.trim());
        }
        if (startDate != null && !startDate.trim().isEmpty()) {
            tasks.setStartDate(startDate.trim());
        }
        if (endDate != null && !endDate.trim().isEmpty()) {
            tasks.setEndDate(endDate.trim());
        }
        return taskRepo.save(tasks);
    }

    public void deleteTask(Long taskId) {
        taskRepo.deleteById(taskId);
    }

    public void markTaskDone(Long taskId) {
        Optional<Task> taskOptional = taskRepo.findById(taskId);
        if(taskOptional.isPresent()) {
            Task task = taskOptional.get();
            task.setStatus("done");
            taskRepo.save(task);
        }
    }


}
