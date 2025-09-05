package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Table(name = "task")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO) // FIXED: Use IDENTITY for MySQL
    @Column(name = "task_id")
    private Long taskId;

    @Column(name = "task_title", nullable = false) // FIXED: Renamed column
    private String task;

    @Column(name = "start_time")
    private String startTime;

    @Column(name = "end_time")
    private String endTime;

    @Column(name = "start_date")
    private String startDate;

    @Column(name = "end_date")
    private String endDate;

    @Column(name = "status", nullable = false)
    @Builder.Default
    private String status = "not done"; // Default value

    @Column(name = "priority", nullable = false)
    @Builder.Default
    private String priority = "medium"; // Default value

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private User assignedUser;

    @Override
    public String toString() {
        return "Task{" +
                "taskId=" + taskId +
                ", task='" + task + '\'' +
                ", status='" + status + '\'' +
                ", priority='" + priority + '\'' +
                ", assignedUser=" + (assignedUser != null ? assignedUser.getUsername() : "null") +
                '}';
    }
}