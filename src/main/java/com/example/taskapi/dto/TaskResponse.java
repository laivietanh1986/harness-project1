package com.example.taskapi.dto;

import com.example.taskapi.entity.Task;

import java.time.Instant;

public class TaskResponse {

    public static TaskResponse from(Task task) {
        return new TaskResponse(task.getId(), task.getTitle(), task.getCategory(), task.getStatus(), task.getCreatedAt());
    }

    private final Long id;
    private final String title;
    private final String category;
    private final String status;
    private final Instant createdAt;

    public TaskResponse(Long id, String title, String category, String status, Instant createdAt) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.status = status;
        this.createdAt = createdAt;
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

    public Instant getCreatedAt() {
        return createdAt;
    }
}
