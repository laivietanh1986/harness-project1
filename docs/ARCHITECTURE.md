# Architecture

- Package layout: `com.example.taskapi.{controller,service,repository,entity,dto,config}`
- Controller should not call direct Repository — need to be over Service
- Entity should not be return by  Controller — use DTO
- H2 config: file mode, path `./data/tasks`, DO NOT use  `jdbc:h2:mem:`

## Logging
- Format: structured JSON (one log event per line) via `logstash-logback-encoder`, configured in `src/main/resources/logback-spring.xml`. Do not log plain-text lines.
- Required fields per log line: `timestamp`, `level`, `logger`, `thread`, `message`, `requestId`, `stack_trace` (on ERROR only).
- Correlation id: `com.example.taskapi.config.RequestLoggingFilter` (a `jakarta.servlet.Filter`) reads/generates `X-Request-Id`, puts it in SLF4J MDC key `requestId` for the life of the request, echoes it back as a response header, and clears the MDC in a `finally` block.
- Per-request access log at INFO: method, path, status, duration (ms) — logged once per request in `RequestLoggingFilter`, not scattered across controllers.
- Validation/4xx failures → WARN. Unhandled exceptions → ERROR with stack trace.
- Never log request/response bodies or task field values — log ids/counts only, to avoid leaking task content into logs.
- Use SLF4J (`LoggerFactory.getLogger(...)`) exclusively — no `System.out`/`System.err`, no `e.printStackTrace()`.

### Business-logic logging (service/controller methods)
- Every method in `TaskService` (and any controller code that makes its own routing/validation decision) logs exactly one line per outcome:
  - **INFO** on success — operation name + identifying context only (task id, list size/count). Never full task titles or full request/response bodies.
  - **WARN** on an expected/client-caused failure (not-found, invalid input) — include the input that caused it (e.g. the requested id) and the reason.
  - **ERROR** on an unexpected failure — log the caught exception object itself (so `stack_trace` is populated), plus the operation/input context needed to reproduce it.
- `requestId` is already in MDC for the whole request (set by `RequestLoggingFilter`), so every business-logic log line is automatically correlated with its access-log line — do not pass `requestId` into service methods manually.
- Log one line per meaningful business decision, not one line per statement — avoid log spam.