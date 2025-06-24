# Job Portal API (Spring Boot)

This is a minimal example of a job portal REST API using Spring Boot. It demonstrates basic CRUD functionality for users, job listings, categories, notifications and file uploads. The application uses an in-memory H2 database and exposes OpenAPI documentation via Swagger UI.

## Features

- User registration and simple basic authentication
- CRUD endpoints for users, job listings, and job categories
- Notification retrieval for users
- File upload and download endpoints
- In-memory H2 database for quick setup
- API documentation with springdoc-openapi (Swagger)

## Running

You can build and run the project with Maven:

```bash
mvn spring-boot:run
```

Then access the API docs at [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html).
