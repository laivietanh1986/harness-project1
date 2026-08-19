# Progress Log

## Current State
- Latest commit: a091dd3 (note: that commit's message claims the F11 endpoint was
  added but its diff only touched src/promts.md — no code was actually implemented
  by it; this session did the real implementation, see F11 below)
- Test status: check_architecture.sh passes; F01-F08, F11 manually verified including
  restart-persistence, structured logging correlation, per-method business-logic
  logging, and filtering/sorting/pagination. init.sh's hardcoded `sleep 15` before
  the health check is flaky on this dev machine (app needs ~18-20s to fully start)
  — reproduced on unmodified HEAD via git stash, so it's a pre-existing environment
  timing issue, not a regression from this session's changes. All init.sh checks
  pass when run manually with a longer startup wait.

## Completed
- Project skeleton: Maven (mvnw/mvnw.cmd generated), Spring Boot 3.3.4, Java 21
- Packages per ARCHITECTURE.md: controller / service / repository / entity / dto / config
- F01: /actuator/health returns 200 {"status":"UP"}
- F02: GET /api/tasks returns JSON array of TaskResponse DTOs
- F03: POST /api/tasks with {"title":...} creates Task, returns 201 + TaskResponse
- F04: H2 file mode at jdbc:h2:file:./data/tasks (ddl-auto=update); verified task
  created before a restart is still present after restarting the app
- F05: POST /api/tasks/import accepts a JSON array of CreateTaskRequest, bulk
  creates via taskRepository.saveAll, returns 201 + List<TaskResponse>; empty
  array returns 201 []; any element with blank/missing title returns 400
  (manual validation in TaskService, not relying on cascaded @Valid on List).
  Verified imported tasks still present via GET /api/tasks and GET /api/tasks/{id}
  after restarting the app.
- F06: GET /api/tasks/{id} returns 200 + TaskResponse, or 404 (via
  ResponseStatusException) when the id doesn't exist
- F07: structured JSON logging — added logstash-logback-encoder dependency,
  src/main/resources/logback-spring.xml (JSON console appender, fields:
  timestamp/level/logger/thread/message/requestId/stack_trace), and
  com.example.taskapi.config.RequestLoggingFilter (OncePerRequestFilter) which
  reads/generates X-Request-Id, puts it in MDC key requestId, echoes it as a
  response header, and logs one line per request (method/path/status/durationMs)
  at INFO/WARN/ERROR based on status code. Verified requestId round-trips between
  request header, response header, and log line; verified auto-generated id when
  header absent; verified 404 logs at WARN.
- Ran ./init.sh end-to-end: build, health check, POST, GET all passed, all app
  log output confirmed as valid JSON lines
- F08: business-logic logging in TaskService — listTasks, createTask, getTask,
  importTasks each log exactly one line per outcome (INFO on success with
  id/count only, WARN on not-found/invalid-input with the offending id/index
  and reason, ERROR wrapping unexpected RuntimeExceptions with the exception
  object so stack_trace populates). No requestId is passed manually — MDC
  correlation from RequestLoggingFilter carries through automatically.
  importTasks validates title-blank per element itself (not via cascaded
  @Valid) so its WARN case lives in the service; createTask's blank-title
  case is rejected by @Valid before TaskService runs, so there is
  intentionally no service-level line for it (the access log still logs
  that 400 at WARN). Verified all success/WARN paths by hitting each
  endpoint with distinct X-Request-Id headers and confirming matching
  requestId between the TaskService log line and the access-log line.

- F11: Task filtering & sorting API (docs/PRODUCT.md "Task Filtering & Sorting
  API" section). GET /api/tasks now accepts category, status (optional, AND-
  combined), sortBy (title|createdAt|category, default createdAt), sortDir
  (asc|desc, default desc), page (default 0), size (default 20). Added
  category/status/createdAt to the Task entity (@PrePersist sets createdAt) and
  to CreateTaskRequest/TaskResponse. Filtering/sorting implemented via
  TaskRepository extends JpaSpecificationExecutor<Task> + a TaskSpecifications
  helper (repository package) combined with Spring Data Sort/PageRequest in
  TaskService.listTasks. Invalid sortBy/sortDir -> 400 via ResponseStatusException
  (logged WARN with the offending value); 0-result filter combos return 200 with
  an empty content array. Response is now PagedTaskResponse
  {content, totalElements, totalPages, page, size} — a static
  TaskResponse.from(Task) factory replaced the old inline mapping (kept a private
  mapper out of TaskService so check_architecture.py's "every service method
  logs" rule doesn't misfire on a pure data mapper).
  IMPORTANT CONTRACT CHANGE: this reuses the same GET /api/tasks endpoint as F02,
  so F02's original "returns a bare JSON array" contract is superseded — it now
  always returns the paginated envelope, even with no query params. User
  confirmed this tradeoff explicitly (asked via AskUserQuestion) rather than
  keeping F02 unchanged behind a separate path. feature_list.json F02 entry
  updated to reflect the new contract.
  Also found (and left as informational, not fixed): the two commits immediately
  before this session ("add evaluator rubric for F11..." and "add task filtering
  and sorting endpoint...") did not actually implement anything — their diffs
  only added evaluator-rubric.md and src/promts.md respectively. The real
  implementation happened in this session.

## In Progress
(empty)

## Next Steps
- All 6 product features (F01-F06) plus F07 (structured logging), F08
  (business-logic logging), and F11 (filtering/sorting/pagination) implemented
  and verified.
- Changes not yet committed to git — ask user before committing/pushing.
- No further scope planned beyond F01-F08, F11.
