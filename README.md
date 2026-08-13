# Task API

Spring Boot REST API for managing a simple task list.

## Stack

- Java 21
- Spring Boot 3.3.4 (Web, Data JPA, Actuator, Validation)
- H2 database, file-based (persists across restarts)
- Maven (wrapper included, no local Maven install required)

## Quick Start

```bash
# Build
./mvnw clean install

# Run (foreground, http://localhost:8080)
./run.sh
# or: ./mvnw spring-boot:run

# Build + start + verify all endpoints, then stop
./init.sh
```

## API

| Method | Path              | Description                          |
|--------|-------------------|---------------------------------------|
| GET    | `/api/tasks`      | List all tasks                        |
| POST   | `/api/tasks`      | Create a task, body `{"title": "..."}`, returns `201` |
| GET    | `/actuator/health`| Health check, returns `200 {"status":"UP"}` when ready |

## Data

Tasks are stored in a file-based H2 database at `./data/tasks.mv.db`. Data
survives application restarts. Delete the `data/` directory to reset state.

## Project Structure

```
src/main/java/com/example/taskapi/
  controller/  REST endpoints
  service/     business logic
  repository/  Spring Data JPA repositories
  entity/      JPA entities (not exposed over the API)
  dto/         request/response objects returned by controllers
```

See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) and
[docs/PRODUCT.md](docs/PRODUCT.md) for design constraints and feature scope.
