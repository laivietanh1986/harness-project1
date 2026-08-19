package com.example.taskapi.service;

import com.example.taskapi.dto.CreateTaskRequest;
import com.example.taskapi.dto.TaskResponse;
import com.example.taskapi.entity.Task;
import com.example.taskapi.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<TaskResponse> listTasks() {
        try {
            List<TaskResponse> tasks = taskRepository.findAll().stream()
                    .map(task -> new TaskResponse(task.getId(), task.getTitle()))
                    .toList();
            log.info("listTasks success count={}", tasks.size());
            return tasks;
        } catch (RuntimeException ex) {
            log.error("listTasks failed", ex);
            throw ex;
        }
    }

    public TaskResponse createTask(CreateTaskRequest request) {
        try {
            Task saved = taskRepository.save(new Task(request.getTitle()));
            log.info("createTask success id={}", saved.getId());
            return new TaskResponse(saved.getId(), saved.getTitle());
        } catch (RuntimeException ex) {
            log.error("createTask failed", ex);
            throw ex;
        }
    }

    public TaskResponse getTask(Long id) {
        try {
            Task task = taskRepository.findById(id)
                    .orElseThrow(() -> {
                        log.warn("getTask not found id={}", id);
                        return new ResponseStatusException(HttpStatus.NOT_FOUND);
                    });
            log.info("getTask success id={}", id);
            return new TaskResponse(task.getId(), task.getTitle());
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            log.error("getTask failed id={}", id, ex);
            throw ex;
        }
    }

    public List<TaskResponse> importTasks(List<CreateTaskRequest> requests) {
        try {
            for (int i = 0; i < requests.size(); i++) {
                CreateTaskRequest request = requests.get(i);
                if (request.getTitle() == null || request.getTitle().isBlank()) {
                    log.warn("importTasks invalid element index={} reason=blank_title", i);
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "title must not be blank");
                }
            }
            List<Task> tasks = requests.stream()
                    .map(request -> new Task(request.getTitle()))
                    .toList();
            List<TaskResponse> saved = taskRepository.saveAll(tasks).stream()
                    .map(task -> new TaskResponse(task.getId(), task.getTitle()))
                    .toList();
            log.info("importTasks success count={}", saved.size());
            return saved;
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            log.error("importTasks failed inputCount={}", requests.size(), ex);
            throw ex;
        }
    }
}
