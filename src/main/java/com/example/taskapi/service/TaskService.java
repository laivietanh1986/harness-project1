package com.example.taskapi.service;

import com.example.taskapi.dto.CreateTaskRequest;
import com.example.taskapi.dto.TaskListResponse;
import com.example.taskapi.dto.TaskResponse;
import com.example.taskapi.entity.Task;
import com.example.taskapi.repository.TaskRepository;
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

    private static final Set<String> ALLOWED_SORT_BY = Set.of("title", "createdAt", "category");
    private static final Set<String> ALLOWED_SORT_DIR = Set.of("asc", "desc");
    private static final String DEFAULT_SORT_BY = "createdAt";
    private static final String DEFAULT_SORT_DIR = "desc";
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public TaskListResponse listTasks(String category, String status, String sortBy, String sortDir,
                                       Integer page, Integer size) {
        String effectiveSortBy = sortBy != null ? sortBy : DEFAULT_SORT_BY;
        String effectiveSortDir = sortDir != null ? sortDir : DEFAULT_SORT_DIR;
        int effectivePage = page != null ? page : DEFAULT_PAGE;
        int effectiveSize = size != null ? size : DEFAULT_SIZE;

        if (!ALLOWED_SORT_BY.contains(effectiveSortBy)) {
            log.warn("listTasks invalid sortBy value={} reason=not_in_allowed_set", effectiveSortBy);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid sortBy '" + effectiveSortBy + "': must be one of " + ALLOWED_SORT_BY);
        }
        if (!ALLOWED_SORT_DIR.contains(effectiveSortDir)) {
            log.warn("listTasks invalid sortDir value={} reason=not_in_allowed_set", effectiveSortDir);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid sortDir '" + effectiveSortDir + "': must be one of " + ALLOWED_SORT_DIR);
        }
        if (effectivePage < 0) {
            log.warn("listTasks invalid page value={} reason=negative", effectivePage);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid page '" + effectivePage + "': must be >= 0");
        }
        if (effectiveSize <= 0) {
            log.warn("listTasks invalid size value={} reason=not_positive", effectiveSize);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid size '" + effectiveSize + "': must be > 0");
        }

        try {
            Sort sort = Sort.by(Sort.Direction.fromString(effectiveSortDir), effectiveSortBy);
            Pageable pageable = PageRequest.of(effectivePage, effectiveSize, sort);
            Specification<Task> spec = TaskSpecifications.filter(category, status);

            Page<Task> result = taskRepository.findAll(spec, pageable);
            List<TaskResponse> content = result.getContent().stream()
                    .map(TaskResponse::from)
                    .toList();

            log.info("listTasks success count={} totalElements={} page={} size={} categoryFilter={} statusFilter={} sortBy={} sortDir={}",
                    content.size(), result.getTotalElements(), effectivePage, effectiveSize,
                    category != null, status != null, effectiveSortBy, effectiveSortDir);

            return new TaskListResponse(content, result.getTotalElements(), result.getTotalPages(),
                    effectivePage, effectiveSize);
        } catch (RuntimeException ex) {
            log.error("listTasks failed categoryFilter={} statusFilter={} sortBy={} sortDir={} page={} size={}",
                    category != null, status != null, effectiveSortBy, effectiveSortDir, effectivePage, effectiveSize, ex);
            throw ex;
        }
    }

    public TaskResponse createTask(CreateTaskRequest request) {
        try {
            Task saved = taskRepository.save(new Task(request.getTitle(), request.getCategory(), request.getStatus()));
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
                    .map(request -> new Task(request.getTitle(), request.getCategory(), request.getStatus()))
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
