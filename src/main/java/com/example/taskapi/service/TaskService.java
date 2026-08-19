package com.example.taskapi.service;

import com.example.taskapi.dto.CreateTaskRequest;
import com.example.taskapi.dto.PagedTaskResponse;
import com.example.taskapi.dto.TaskResponse;
import com.example.taskapi.entity.Task;
import com.example.taskapi.repository.TaskRepository;
import com.example.taskapi.repository.TaskSpecifications;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;

@Service
public class TaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("title", "createdAt", "category");

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public PagedTaskResponse listTasks(String category, String status, String sortBy, String sortDir, int page, int size) {
        try {
            if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
                log.warn("listTasks invalid sortBy value={}", sortBy);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "invalid sortBy '" + sortBy + "', allowed values: title, createdAt, category");
            }
            Sort.Direction direction;
            try {
                direction = Sort.Direction.fromString(sortDir);
            } catch (IllegalArgumentException ex) {
                log.warn("listTasks invalid sortDir value={}", sortDir);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "invalid sortDir '" + sortDir + "', allowed values: asc, desc");
            }
            if (page < 0) {
                log.warn("listTasks invalid page value={}", page);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "page must be >= 0");
            }
            if (size < 1) {
                log.warn("listTasks invalid size value={}", size);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "size must be >= 1");
            }

            boolean hasCategory = category != null && !category.isBlank();
            boolean hasStatus = status != null && !status.isBlank();
            Specification<Task> spec = Specification.where(null);
            if (hasCategory) {
                spec = spec.and(TaskSpecifications.hasCategory(category));
            }
            if (hasStatus) {
                spec = spec.and(TaskSpecifications.hasStatus(status));
            }

            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            Page<Task> result = taskRepository.findAll(spec, pageable);
            List<TaskResponse> content = result.getContent().stream()
                    .map(TaskResponse::from)
                    .toList();

            log.info("listTasks success count={} totalElements={} page={} size={} hasCategoryFilter={} hasStatusFilter={}",
                    content.size(), result.getTotalElements(), page, size, hasCategory, hasStatus);
            return new PagedTaskResponse(content, result.getTotalElements(), result.getTotalPages(), page, size);
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            log.error("listTasks failed sortBy={} sortDir={} page={} size={}", sortBy, sortDir, page, size, ex);
            throw ex;
        }
    }

    public TaskResponse createTask(CreateTaskRequest request) {
        try {
            Task task = new Task(request.getTitle());
            task.setCategory(request.getCategory());
            task.setStatus(request.getStatus());
            Task saved = taskRepository.save(task);
            log.info("createTask success id={}", saved.getId());
            return TaskResponse.from(saved);
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
            return TaskResponse.from(task);
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
                    .map(request -> {
                        Task task = new Task(request.getTitle());
                        task.setCategory(request.getCategory());
                        task.setStatus(request.getStatus());
                        return task;
                    })
                    .toList();
            List<TaskResponse> saved = taskRepository.saveAll(tasks).stream()
                    .map(TaskResponse::from)
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
