package com.example.taskapi.dto;

import com.example.taskapi.entity.Task;

import java.time.LocalDateTime;

public class TaskResponse {

    private final Long id;
    private final String title;
    private final String category;
    private final String status;
    private final LocalDateTime createdAt;

    public TaskResponse(Long id, String title, String category, String status, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static TaskResponse from(Task task) {
        return new TaskResponse(task.getId(), task.getTitle(), task.getCategory(), task.getStatus(), task.getCreatedAt());
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
