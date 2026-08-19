package com.example.taskapi.controller;

import com.example.taskapi.dto.CreateTaskRequest;
import com.example.taskapi.dto.TaskListResponse;
import com.example.taskapi.dto.TaskResponse;
import com.example.taskapi.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/api/tasks")
    public TaskListResponse listTasks(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDir,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return taskService.listTasks(category, status, sortBy, sortDir, page, size);
    }

    @GetMapping("/api/tasks/{id}")
    public TaskResponse getTask(@PathVariable Long id) {
        return taskService.getTask(id);
    }

    @PostMapping("/api/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse createTask(@Valid @RequestBody CreateTaskRequest request) {
        return taskService.createTask(request);
    }

    @PostMapping("/api/tasks/import")
    @ResponseStatus(HttpStatus.CREATED)
    public List<TaskResponse> importTasks(@RequestBody List<CreateTaskRequest> requests) {
        return taskService.importTasks(requests);
    }
}
