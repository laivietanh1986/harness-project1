package com.example.taskapi.service;

import com.example.taskapi.dto.CreateTaskRequest;
import com.example.taskapi.dto.TaskResponse;
import com.example.taskapi.entity.Task;
import com.example.taskapi.repository.TaskRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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

    public TaskResponse getTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return new TaskResponse(task.getId(), task.getTitle());
    }

    public List<TaskResponse> importTasks(List<CreateTaskRequest> requests) {
        for (CreateTaskRequest request : requests) {
            if (request.getTitle() == null || request.getTitle().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "title must not be blank");
            }
        }
        List<Task> tasks = requests.stream()
                .map(request -> new Task(request.getTitle()))
                .toList();
        return taskRepository.saveAll(tasks).stream()
                .map(task -> new TaskResponse(task.getId(), task.getTitle()))
                .toList();
    }
}
