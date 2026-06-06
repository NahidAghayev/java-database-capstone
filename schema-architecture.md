# Smart Clinic Management System — Schema & Architecture

## Section 1: Architecture Summary

The Smart Clinic Management System is a full-stack Java application built with Spring Boot 3.4.4 and a dual-database design — MySQL (via Spring Data JPA / Hibernate) for transactional relational data (patients, doctors, admins, appointments) and MongoDB for document-based prescription records. The frontend uses vanilla JavaScript ES modules with static HTML pages, two Thymeleaf templates for admin and doctor dashboards, and communicates with the backend through REST APIs secured by custom JWT token authentication. The system follows a layered architecture (Controller → Service → Repository → Database) with four distinct user roles: Admin, Doctor, Patient, and Logged-in Patient, each with role-specific views and capabilities.

## Section 2: Data & Control Flow

1. **User Interaction** — A user (Admin, Doctor, or Patient) interacts with an HTML page served from the `static/` directory (or a Thymeleaf template for admin/doctor dashboards). JavaScript modules in `js/services/` capture form input or button clicks and construct API requests.

2. **API Call with Authentication** — The frontend calls a REST endpoint via `fetch()` with the JWT token (stored in `localStorage`) appended as a path variable or header. API base URL is configured in `js/config/config.js` (`http://localhost:8080`).

3. **Controller Reception** — A `@RestController` (e.g., `PatientController`, `AppointmentController`) receives the HTTP request, extracts the token and payload, and delegates to the service layer. The `@RestControllerAdvice` handler (`ValidationFailed`) catches any validation errors before business logic runs.

4. **Token Validation & Authorization** — The controller or service calls `Service.validateToken()` → `TokenService.validateToken()` which decodes the JWT, extracts the email, and looks up the user in the appropriate repository to confirm the token is valid and the user exists.

5. **Business Logic Execution** — The service layer (e.g., `AppointmentService`, `PatientService`, `PrescriptionService`) executes the core business logic — validating constraints (e.g., duplicate email, doctor availability, time slot conflicts), processing data transformations (e.g., converting entities to DTOs), and coordinating across repositories when needed.

6. **Data Persistence or Retrieval** — The service calls the appropriate repository — a Spring Data JPA repository (e.g., `PatientRepository`, `AppointmentRepository`) for MySQL operations, or `PrescriptionRepository` (a Spring Data MongoDB repository) for prescription documents. Hibernate auto-generates SQL queries from method names or `@Query` annotations.

7. **Response & Rendering** — The repository returns entities/documents to the service, which processes them (possibly converting to DTOs like `AppointmentDTO`) and returns them to the controller. The controller sends the JSON response back to the frontend, where the JavaScript service module receives it and updates the DOM accordingly (e.g., rendering patient rows, doctor cards, or appointment tables via components in `js/components/`).
