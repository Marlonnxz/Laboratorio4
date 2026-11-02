# Enrollments Service (`enrollments-service`)

**Distributed Systems (Sistemas Distribuidos) — UPTC**  
**Role & Technical Lead:** Marlon (Enrollments + Integration Lead)

---

## 1. Overview & Architectural Principles

The `enrollments-service` is an autonomous, domain-driven microservice responsible for student course enrollments, academic grading lifecycle, and status audit histories within the modular university platform.

### Non-Negotiable Distributed Architecture Rules:
1. **Absolute Autonomy & Decoupling:**
   - Possesses its own isolated repository, lifecycle, and dedicated database (`enrollments_db`).
   - Zero class imports, packages, or entities from `students-service` or `subjects-service`.
   - External references (`studentId`, `courseId`) are handled strictly as primitive `Long` numbers without foreign keys or relational database cross-coupling.
2. **Replicated Reference Data:**
   - The academic period attribute (e.g., `"2026-2"`) is a local string representation managed directly by this service without relying on external centralized tables.
3. **Automated Audit Lifecycle:**
   - Whenever an enrollment is created or its `status` is transitioned (`ACTIVE`, `CANCELLED`, `COMPLETED`), an immutable entry is automatically generated and persisted in `EnrollmentStatusHistory` with an execution timestamp and mandatory reason.
4. **Defensive Validation & Business Rules:**
   - Grade values are strictly restricted to the range `0.0` to `5.0`.
   - Grade percentage values are restricted to `0.0%` to `100.0%`.
   - The cumulative percentage of all grades belonging to an enrollment cannot exceed `100.0%`.

---

## 2. Tech Stack

- **Language:** Java 21 LTS
- **Framework:** Spring Boot 3.3.4
- **Persistence:** Spring Data JPA / Hibernate
- **Database:** PostgreSQL 16 (Port: `5434` host, `5432` container)
- **API Documentation:** Springdoc OpenAPI 3 / Swagger UI (`springdoc-openapi-starter-webmvc-ui` 2.6.0)
- **Containerization:** Docker & Docker Compose
- **Testing:** JUnit 5, Mockito, Spring Boot Test (`MockMvc`), H2 In-Memory DB for standalone test execution

---

## 3. Port & Service Allocation

| Component | Host Port | Internal Docker Port | Database / Target |
|---|---|---|---|
| **`enrollments-service`** | `8083` | `8083` | HTTP REST API & Swagger UI |
| **`enrollments-db`** | `5434` | `5432` | PostgreSQL (`enrollments_db`) |
| **API Gateway** | `8080` | `8080` | Spring Cloud Gateway |

---

## 4. REST API Specification

### 4.1. Enrollments (`/api/enrollments`)

| Method | Endpoint | Description | Query Parameters / Body |
|---|---|---|---|
| `POST` | `/api/enrollments` | Register a new enrollment | Body: `CreateEnrollmentRequest` (Status history auto-created) |
| `GET` | `/api/enrollments` | Dynamic search with pagination & filters | `studentId`, `courseId`, `status`, `period`, `pageNumber`, `pageSize`, `sortBy`, `sortDirection`, `unpaged` |
| `GET` | `/api/enrollments/{id}` | Retrieve enrollment details by ID | Includes associated grades and status audit history |
| `PUT` | `/api/enrollments/{id}` | Update enrollment | Body: `UpdateEnrollmentRequest` (Requires `reason` if status changes) |
| `DELETE` | `/api/enrollments/{id}` | Delete enrollment record | Cascades deletion to grades and status history |

#### API Gateway `/detalle` Critical Contract:
When the API Gateway composes `/api/estudiantes/{id}/detalle`, it invokes:
```http
GET http://enrollments-service:8083/api/enrollments?studentId=12
```
Or with `unpaged=true` to guarantee zero truncation:
```http
GET http://enrollments-service:8083/api/enrollments?studentId=12&unpaged=true
```
**Response Format:**
```json
{
  "content": [
    {
      "id": 45,
      "studentId": 12,
      "courseId": 7,
      "period": "2026-2",
      "status": "ACTIVE",
      "enrollmentDate": "2026-08-20",
      "grades": [
        {
          "id": 1,
          "enrollmentId": 45,
          "type": "Midterm 1",
          "value": 4.20,
          "percentage": 30.00
        },
        {
          "id": 2,
          "enrollmentId": 45,
          "type": "Midterm 2",
          "value": 3.80,
          "percentage": 30.00
        },
        {
          "id": 3,
          "enrollmentId": 45,
          "type": "Final Exam",
          "value": 4.50,
          "percentage": 40.00
        }
      ]
    }
  ],
  "pageNumber": 0,
  "pageSize": 10,
  "totalElements": 1,
  "totalPages": 1
}
```

### 4.2. Grades (`/api/grades`)

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/grades` | Add a grade to an enrollment (Validates 0.0-5.0 and total <= 100%) |
| `GET` | `/api/grades?enrollmentId={id}` | List grades with pagination |
| `GET` | `/api/grades/{id}` | Get grade by ID |
| `PUT` | `/api/grades/{id}` | Update grade (Validates total <= 100%) |
| `DELETE` | `/api/grades/{id}` | Delete grade |

### 4.3. Status Audit History (`/api/enrollment-status-history`)

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/enrollment-status-history` | Manually log a status transition |
| `GET` | `/api/enrollment-status-history?enrollmentId={id}` | Query audit entries with pagination |
| `GET` | `/api/enrollment-status-history/{id}` | Get audit entry by ID |

---

## 5. Standard Error Format

All error responses strictly match the unified architecture contract:
```json
{
  "timestamp": "2026-09-20T18:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Enrollment with id 999 was not found",
  "path": "/api/enrollments/999"
}
```

---

## 6. How to Run

### Option A: Running with Docker Compose (Recommended)

1. Make sure Docker is running.
2. In the `enrollments-service` directory, execute:
   ```bash
   docker compose up --build
   ```
3. This command builds the Java 21 multi-stage image, launches PostgreSQL on host port `5434`, initializes the database schema, seeds `data.sql`, and exposes the service at `http://localhost:8083`.

### Option B: Running Locally with Maven

1. Start PostgreSQL (e.g., via Docker or local installation) with:
   - Database: `enrollments_db`
   - Port: `5434`
   - User: `postgres` / Password: `postgres`
2. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```
   *(Or `mvn spring-boot:run`)*

---

## 7. Interactive Documentation (Swagger UI) & Healthcheck

- **Swagger UI:** [http://localhost:8083/swagger-ui/index.html](http://localhost:8083/swagger-ui/index.html)
- **OpenAPI 3 JSON Spec:** [http://localhost:8083/v3/api-docs](http://localhost:8083/v3/api-docs)
- **Actuator Health Check:** [http://localhost:8083/actuator/health](http://localhost:8083/actuator/health)

---

## 8. Automated Testing

Run the test suite executing unit tests and WebMvc integration tests:
```bash
./mvnw test
```
*(Uses an embedded in-memory H2 database with PostgreSQL compatibility mode, running isolated from any active database instance).*
