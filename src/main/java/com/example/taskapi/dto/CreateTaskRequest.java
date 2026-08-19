package com.example.taskapi.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateTaskRequest {

    @NotBlank
    private String title;

    private String category;

    private String status;

    public CreateTaskRequest() {
    }

    public CreateTaskRequest(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
