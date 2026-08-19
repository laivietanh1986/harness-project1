# Progress Log

## Current State
- Latest commit: acf40c3 (structured logging with request correlation ID)
- Test status: init.sh passes; F01-F08 manually verified including restart-persistence,
  structured logging correlation, and per-method business-logic logging

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

## In Progress
(empty)

## Next Steps
- All 6 product features (F01-F06) plus F07 (structured logging) and F08
  (business-logic logging) implemented and verified.
- Changes not yet committed to git — ask user before committing/pushing.
- No further scope planned beyond F01-F08.
