package com.example.taskapi.service;

import com.example.taskapi.dto.CreateTaskRequest;
import com.example.taskapi.dto.TaskResponse;
import com.example.taskapi.entity.Task;
import com.example.taskapi.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<TaskResponse> listTasks() {
        return taskRepository.findAll().stream()
                .map(task -> new TaskResponse(task.getId(), task.getTitle()))
                .toList();
    }

    public TaskResponse createTask(CreateTaskRequest request) {
        Task saved = taskRepository.save(new Task(request.getTitle()));
        return new TaskResponse(saved.getId(), saved.getTitle());
    }
}
