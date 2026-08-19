# Progress Log

## Current State
- Latest commit: a091dd3 (adds src/promts.md, the F11 task prompt only — no F11 code was
  committed yet). F11 implementation below is on top of this, not yet committed.
- Test status: init.sh passes; F01-F08 manually verified including restart-persistence,
  structured logging correlation, and per-method business-logic logging. F11 verified via
  11 automated MockMvc tests (all passing) plus manual curl checks.

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
- F11: Task Filtering & Sorting API, implemented per docs/SPRINT_CONTRACT_F11.md
  (canonical numbering F11, since the uncommitted PRODUCT.md section mislabels this
  "F07", colliding with the existing F07 structured-logging feature — contract flags
  this but intentionally does not rewrite PRODUCT.md's header).
  - Task entity gained category (String, nullable), status (String, nullable),
    createdAt (LocalDateTime, set once via @PrePersist, not updatable). ddl-auto=update
    adds these as nullable columns; pre-existing rows get NULL, handled null-safely.
  - CreateTaskRequest/TaskResponse gained optional category/status fields (free-form
    strings, no enum enforcement); TaskResponse also gained createdAt and a static
    TaskResponse.from(Task) factory to avoid duplicating the mapping across
    listTasks/createTask/getTask/importTasks.
  - TaskRepository now extends JpaSpecificationExecutor<Task>. New package-private
    TaskSpecifications class (service package, no @Service — pure Specification
    builders, not subject to the per-method logging check) provides hasCategory/
    hasStatus/filter, combined with AND, null-safe (a null/blank filter contributes
    no predicate).
  - TaskService.listTasks(category, status, sortBy, sortDir, page, size) replaces the
    old no-arg listTasks(): applies defaults (sortBy=createdAt, sortDir=desc, page=0,
    size=20), validates sortBy against {title, createdAt, category} and sortDir
    against {asc, desc} case-sensitively, validates page>=0 and size>0, builds a
    Specification + Pageable, and returns a new TaskListResponse DTO
    {content, totalElements, totalPages, page, size}. Logs one INFO line per success
    with count/totalElements/page/size/sortBy/sortDir plus boolean
    categoryFilter/statusFilter flags (not the actual filter values, since those are
    arbitrary user content per ARCHITECTURE.md's logging rule); logs one WARN line per
    invalid sortBy/sortDir/page/size with the offending value and reason.
  - TaskController's GET /api/tasks now takes category/status/sortBy/sortDir/page/size
    as optional @RequestParam and delegates straight to the service — no query logic
    in the controller.
  - GET /api/tasks response shape changed from a plain JSON array to the paginated
    envelope required by the contract; this is an intentional, contract-specified
    change to the endpoint's response format, not a regression of F02.
  - Tests: src/test/java/com/example/taskapi/TaskFilteringSortingTest.java, an
    @SpringBootTest + @AutoConfigureMockMvc test class (11 cases) covering: defaults
    with no params, filter by category alone, filter by status alone, combined
    category+status (AND), a 0-result filter combination (200 + empty content),
    sortBy=title/sortDir=asc ordering, invalid sortBy -> 400, invalid sortDir -> 400,
    negative page -> 400, non-positive size -> 400, and pagination metadata across
    multiple pages. Tests run against an in-memory H2 DB
    (src/test/resources/application.properties) so they don't touch the dev
    ./data/tasks file; this is test-only and doesn't change the app's persistent
    H2-file-mode configuration.
  - Verified: `./mvnw clean test` (11/11 passed), `./check_architecture.sh` (OK),
    `./init.sh` end-to-end (build + health + POST/GET smoke checks against the new
    response shape), and manual curl checks of category+status AND filtering,
    sortBy/sortDir ordering, invalid sortBy/sortDir/page (400), and a zero-result
    filter (200, empty content).

## In Progress
(empty)

## Next Steps
- All 6 product features (F01-F06), F07 (structured logging), F08 (business-logic
  logging), and F11 (task filtering & sorting) implemented and verified.
- Changes not yet committed to git — ask user before committing/pushing.
- Flag for follow-up (out of scope for F11 itself, per its contract §1): PRODUCT.md's
  uncommitted "Task Filtering & Sorting API" section is titled "F07", colliding with
  the existing F07 (structured logging) entry in feature_list.json. The canonical
  feature_list.json entry was added as F11 per the contract and evaluator-rubric.md;
  the PRODUCT.md header still needs a one-line fix to say F11 instead of F07.
- No further scope planned beyond F01-F08 + F11.
