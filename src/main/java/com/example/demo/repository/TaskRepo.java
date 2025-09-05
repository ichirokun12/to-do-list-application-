package com.example.demo.repository;

import com.example.demo.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepo extends JpaRepository<Task, Long> {

    // Find tasks by user ID
    List<Task> findByAssignedUser_UserId(Long userId);

    // Find tasks by status
    List<Task> findByStatus(String status);

    // Find tasks by priority
    List<Task> findByPriority(String priority);

    // Find tasks by user ID and status
    List<Task> findByAssignedUser_UserIdAndStatus(Long userId, String status);

    // Count tasks by user ID
    Long countByAssignedUser_UserId(Long userId);

    // Count tasks by user ID and status
    Long countByAssignedUser_UserIdAndStatus(Long userId, String status);

    // Find tasks by user ID and priority
    List<Task> findByAssignedUser_UserIdAndPriority(Long userId, String priority);

    // Find tasks by user ID ordered by creation date
    List<Task> findByAssignedUser_UserIdOrderByTaskIdDesc(Long userId);

    // Find tasks by task name containing (for search functionality)
    List<Task> findByTaskContainingIgnoreCase(String taskName);

    // Find tasks by user ID and task name containing
    List<Task> findByAssignedUser_UserIdAndTaskContainingIgnoreCase(Long userId, String taskName);
}