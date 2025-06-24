# Job Portal Microservices

This folder contains a basic microservice split of the job portal example. Each service is a standalone Spring Boot application using an H2 in-memory database and Swagger documentation.

## Services
- **user-service** – manages user registration and CRUD
- **job-service** – handles job listings and categories
- **notification-service** – stores and retrieves notifications
- **file-service** – file upload/download endpoints

Run each service individually with `mvn spring-boot:run` inside its directory.
