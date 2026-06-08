# Smart Clinic Management System

**IBM Java Development Capstone Project**

A full-stack clinic management application built with Spring Boot, featuring dual-database architecture (MySQL + MongoDB), JWT-based authentication, role-based dashboards, and RESTful APIs.

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [System Architecture](#system-architecture)
- [Database Design](#database-design)
- [User Roles](#user-roles)
- [Getting Started](#getting-started)
- [API Endpoints](#api-endpoints)
- [Project Structure](#project-structure)
- [CI/CD](#cicd)

## Features

- **Patient Management** — Sign up, log in, browse doctors, book/reschedule/cancel appointments, view appointment history
- **Doctor Management** — Admin adds/manages doctors with specializations and available time slots; doctors view daily appointments and manage prescriptions
- **Appointment Scheduling** — Book appointments with real-time availability checking; filter by doctor, specialty, and time
- **Prescription Management** — Doctors create and view prescriptions (stored in MongoDB); linked to appointments for full medical history
- **Role-Based Dashboards** — Thymeleaf-rendered dashboards for Admin and Doctor; static HTML pages for patients
- **JWT Authentication** — Secure token-based authentication for all user roles
- **Dual Database** — MySQL for relational data (patients, doctors, appointments) and MongoDB for document-based prescription records
- **Docker Support** — Containerized deployment with MySQL, MongoDB, and the Spring Boot application

## Tech Stack

| Layer | Technology |
|-------|-----------|
| **Backend** | Java 17, Spring Boot 3.4.4, Spring Data JPA, Spring Web |
| **Frontend** | HTML5, CSS3, Vanilla JavaScript (ES Modules), Thymeleaf |
| **Relational DB** | MySQL 8.0 (via Hibernate/JPA) |
| **Document DB** | MongoDB 6.0 (via Spring Data MongoDB) |
| **Authentication** | Custom JWT (jjwt 0.12.6) |
| **Validation** | Jakarta Bean Validation |
| **Build Tool** | Maven |
| **Containerization** | Docker, Docker Compose |
| **CI/CD** | GitHub Actions |

## System Architecture

The application follows a layered architecture:

```
┌─────────────────────────────────────────────────┐
│                   Frontend                       │
│  ┌──────────────┐  ┌─────────────────────────┐  │
│  │ Static HTML  │  │ Thymeleaf Dashboards    │  │
│  │ (ES Modules) │  │ (Admin / Doctor)        │  │
│  └──────┬───────┘  └──────────┬──────────────┘  │
│         │                     │                  │
│         └──────────┬──────────┘                  │
│                    │ fetch() + JWT               │
├────────────────────┼─────────────────────────────┤
│              REST API (JSON)                     │
├────────────────────┼─────────────────────────────┤
│                 Backend                          │
│  ┌──────────────────────────────────────────┐   │
│  │  Controllers  (REST / MVC)               │   │
│  ├──────────────────────────────────────────┤   │
│  │  Services     (Business Logic)           │   │
│  ├──────────────────────────────────────────┤   │
│  │  Repositories (JPA / MongoDB)            │   │
│  └──────────┬─────────────────────┬─────────┘   │
│             │                     │              │
│        ┌────┴─────┐         ┌────┴─────┐        │
│        │  MySQL   │         │ MongoDB  │        │
│        │ (JPA)    │         │ (Spring  │        │
│        │          │         │  Data)   │        │
│        └──────────┘         └──────────┘        │
└─────────────────────────────────────────────────┘
```

Data flows:
1. The frontend (static HTML or Thymeleaf) sends `fetch()` requests with JWT tokens
2. Controllers receive requests, validate tokens via `CommonService`, and delegate to service layer
3. Services execute business logic and interact with repositories
4. Repositories persist/retrieve data from MySQL (via JPA) or MongoDB
5. Responses flow back as JSON to the frontend for DOM updates

## Database Design

### MySQL (Relational)

| Table | Key Fields |
|-------|-----------|
| `patients` | id, name, email (unique), password, phone, address |
| `doctors` | id, name, specialty, email (unique), password, phone, available_times |
| `admin` | id, username, password |
| `appointments` | id, doctor_id (FK), patient_id (FK), appointment_time, status |
| `clinic_locations` | id, name, address, phone, email |

### MongoDB (Document)

| Collection | Structure |
|-----------|-----------|
| `prescriptions` | appointmentId, patientName, medication[], dosage, doctorNotes, refillCount, tags[], pharmacy{}, metadata{} |

## User Roles

| Role | Capabilities |
|------|-------------|
| **Admin** | Login, add/edit/delete doctors, view all doctors, filter doctors by name/specialty/time |
| **Patient** | Sign up, login, browse/filter doctors, book/reschedule/cancel appointments, view appointment history |
| **Doctor** | Login, view daily appointments, search patients, view medical records, create/view prescriptions |

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.9+
- MySQL 8.0+
- MongoDB 6.0+
- Docker (optional, for containerized setup)

### Run with Docker

```bash
cd app
docker compose up
```

The application starts at `http://localhost:8080`.

### Run without Docker

1. **Start MySQL and MongoDB** on your local machine.

2. **Configure connection strings** in `app/src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/cms
   spring.datasource.username=root
   spring.datasource.password=<your_password>
   spring.data.mongodb.uri=mongodb://localhost:27017/prescriptions
   ```

3. **Build and run**:
   ```bash
   cd app
   mvn clean package -DskipTests
   java -jar target/back-end-0.0.1-SNAPSHOT.jar
   ```

## API Endpoints

### Patient

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/patient` | Create a new patient account |
| POST | `/patient/login` | Patient login (returns JWT) |
| GET | `/patient/{token}` | Get patient details |
| GET | `/patient/{id}/{user}/{token}` | Get patient appointments by ID |
| GET | `/patient/filter/{condition}/{name}/{token}` | Filter patient appointments |

### Doctor

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/doctor/getDoctors` | List all doctors |
| GET | `/doctor/filter/{name}/{time}/{speciality}` | Filter doctors |
| POST | `/doctor/login` | Doctor login |
| POST | `/doctor/saveDoctor/{token}` | Add a new doctor (admin) |
| PUT | `/doctor/updateDoctor/{token}` | Update doctor details (admin) |
| DELETE | `/doctor/deleteDoctor/{id}/{token}` | Delete a doctor (admin) |
| GET | `/{user}/getAvailability/{doctorId}/{date}/{token}` | Get doctor's available time slots |

### Appointment

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/appointments/{token}` | Book an appointment |
| PUT | `/appointments/{token}` | Update/reschedule an appointment |
| DELETE | `/appointments/cancel/{id}/{token}` | Cancel an appointment |
| GET | `/{date}/{patientName}/{token}` | Get appointments by date and patient (doctor) |

### Prescription

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/prescription/{id}/{token}` | Get prescription by appointment ID |
| POST | `/prescription/{token}` | Create a new prescription |
| DELETE | `/prescription/{id}/{token}` | Delete a prescription |

### Admin

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/admin/login` | Admin login |

## Project Structure

```
├── app/
│   ├── Dockerfile
│   ├── docker-compose.yaml
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/project/back_end/
│       │   ├── BackEndApplication.java
│       │   ├── config/         # App configuration
│       │   ├── controllers/    # REST and MVC controllers
│       │   ├── DTO/            # Data transfer objects
│       │   ├── models/         # JPA entities and MongoDB documents
│       │   ├── mvc/            # Thymeleaf MVC controllers
│       │   ├── repo/           # Spring Data repositories
│       │   └── services/       # Business logic layer
│       └── resources/
│           ├── static/         # Static assets (HTML, CSS, JS, images)
│           │   ├── assets/
│           │   ├── js/
│           │   └── pages/
│           ├── templates/      # Thymeleaf templates
│           └── application.properties
├── .github/workflows/          # CI/CD pipelines
├── schema-architecture.md      # Architecture documentation
├── schema-design.md            # Database schema documentation
├── user_stories.md             # User stories
└── README.md
```

## CI/CD

GitHub Actions workflows are configured for automated quality checks:

| Workflow | Description |
|----------|-------------|
| `lint-frontend.yml` | Lints HTML, CSS, and JavaScript with htmlhint, stylelint, and ESLint |
| `lint-backend.yml` | Lints Java code with Checkstyle (Google style) |
| `lint-docker.yml` | Lints Dockerfile with hadolint |
| `compile-backend.yml` | Compiles the Maven project to verify the build |

## License

This project is part of the **IBM Java Development Capstone** and is provided for educational purposes.
