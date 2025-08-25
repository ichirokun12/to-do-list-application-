package com.example.demo.entity;


import jakarta.persistence.*;
import lombok.*;
import org.springframework.stereotype.Component;

@Table(name = "task")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Component
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long taskId;
    @Column(name = "tasks")
    private String task;
    @Column(name = "start_time")
    private String startTime;
    @Column(name = "end_time")
    private String endTime;
    @Column(name = "start_date")
    private String startDate;
    @Column(name = "end_date")
    private String endDate;
    @Column(name = "status")
    private String status;
    @Column(name = "priority")
    private String priority;

}
