# VIETANH.md
## project overview
Spring boot 3.x Rest API Task management , Java 21, Maven , H2 file-based DB
## Quick Start
- Build : './mvnw clean install'
- Run : './mvnw spring-boot:run'
- verify : './init.sh'
## Hard constraints
- data need to be persist after restart (H2 mode  not in-memory mode)
- Health check endpoint need to be return 200 when app ready
- DTO seperate with Entity , do not expose JPA entity over controller
- Logging must be structured (JSON), with a request correlation id (requestId) on every log line — see docs/ARCHITECTURE.md#logging. No System.out/System.err, use SLF4J Logger only.
- Every service/controller method must log: INFO on success (operation + identifying context, e.g. task id/count) and WARN/ERROR on failure (operation + input context + the exception) — enough to diagnose the bug from the log alone, without full task titles/bodies. See docs/ARCHITECTURE.md#logging.
## Read before code
- docs/ARCHITECTURE.md - package structure , layer boundary
- docs/PRODUCT.md - describe 4 feature need to be done and acceptance criterial
- feature_list.json -state of each feature , update after finish
- claude-progress.md - progress history , read first each session  and update after each session .

