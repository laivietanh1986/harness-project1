# Progress Log

## Current State
- Latest commit: a520a77 (add readme)
- Test status: init.sh passes; F01-F06 manually verified including restart-persistence

## Completed
- Project skeleton: Maven (mvnw/mvnw.cmd generated), Spring Boot 3.3.4, Java 21
- Packages per ARCHITECTURE.md: controller / service / repository / entity / dto
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
- Ran ./init.sh end-to-end: build, health check, POST, GET all passed

## In Progress
(empty)

## Next Steps
- All 6 features (F01-F06) implemented and verified per docs/PRODUCT.md.
- Changes not yet committed to git — ask user before committing/pushing.
- No further scope planned beyond the 6 listed features.
