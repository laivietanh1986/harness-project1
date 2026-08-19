# Progress Log

## Current State
- Latest commit: a520a77 (add readme)
- Test status: init.sh passes; F01-F07 manually verified including restart-persistence
  and structured logging correlation

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

## In Progress
(empty)

## Next Steps
- All 6 product features (F01-F06) plus F07 (structured logging) implemented
  and verified.
- Changes not yet committed to git — ask user before committing/pushing.
- No further scope planned beyond F01-F07.
