# Architecture

- Package layout: `com.example.taskapi.{controller,service,repository,entity,dto}`
- Controller should not call direct Repository — need to be over Service
- Entity should not be return by  Controller — use DTO
- H2 config: file mode, path `./data/tasks`, DO NOT use  `jdbc:h2:mem:`