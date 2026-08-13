Build a Spring Boot REST API for managing a simple task list.

Requirements:
- GET /api/tasks returns the list of tasks as JSON
- POST /api/tasks with {"title": "..."} creates a new task and returns 201
- Data must persist locally (survive app restart) using a file-based H2 database
- The app must expose a health check endpoint that returns 200 when running

Use Java 21 and Maven.