# students-service

Microservicio de gestión de estudiantes — Proyecto de Sistemas Distribuidos (UPTC).

Responsable de los datos de **Student**, **Program**, **Contact** y **Document**, según la Especificación Técnica v2 acordada por el equipo. Este módulo no depende de ningún otro servicio ni comparte base de datos con ellos.

## Stack tecnológico

- Java 21
- Spring Boot 3.3.4
- Spring Data JPA / Hibernate
- PostgreSQL 16
- Maven
- springdoc-openapi (Swagger UI)
- JUnit 5 + Mockito
- Docker / Docker Compose

## Puertos

| Servicio | Puerto |
|---|---|
| students-service | 8081 |
| students-db (PostgreSQL) | 5432 |

## Cómo ejecutar el proyecto

### Opción A — Docker (recomendada)

Prerrequisito: tener [Docker Desktop](https://www.docker.com/products/docker-desktop/) instalado y corriendo.

```bash
git clone <url-del-repositorio>
cd students-service
docker compose up --build
```

Esto levanta dos contenedores:
- `students-db`: PostgreSQL con los datos de prueba precargados (20 estudiantes, 5 programas, contactos y documentos).
- `students-service`: la API, expuesta en `http://localhost:8081`.

Para apagarlo: `Ctrl+C` en la misma terminal (apagado ordenado), o `docker compose down` desde otra terminal.

> No necesitas tener Java ni Maven instalados para esta opción — Docker construye todo dentro de su propio entorno.

### Opción B — Local (sin Docker)

Prerrequisitos: JDK 21, Maven, y una instancia de PostgreSQL corriendo con una base `students_db`.

Variables de entorno (opcionales, tienen valores por defecto para desarrollo local):

| Variable | Default |
|---|---|
| `DB_HOST` | `localhost` |
| `DB_PORT` | `5432` |
| `DB_NAME` | `students_db` |
| `DB_USERNAME` | `postgres` |
| `DB_PASSWORD` | `postgres` |

```bash
./mvnw spring-boot:run
```

## Documentación de la API

- Swagger UI: http://localhost:8081/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8081/v3/api-docs
- Health check: http://localhost:8081/actuator/health

## Endpoints

### Students — `/api/students`

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/students` | Lista paginada, ordenable y filtrable |
| GET | `/api/students/{id}` | Obtener por id |
| POST | `/api/students` | Crear |
| PUT | `/api/students/{id}` | Actualizar |
| DELETE | `/api/students/{id}` | Eliminar |

**Parámetros de `GET /api/students`:**

```
pageNumber, pageSize, sortBy, sortDirection, firstName, lastName, programId, email
```

Ejemplo:
```
GET /api/students?pageNumber=0&pageSize=10&sortBy=lastName&sortDirection=asc&firstName=Laura&programId=2
```

### Programs — `/api/programs`
CRUD completo (`GET`, `GET /{id}`, `POST`, `PUT /{id}`, `DELETE /{id}`), paginado con `pageNumber`/`pageSize`.

### Contacts — `/api/contacts`
CRUD completo. Un `Student` solo puede tener un `Contact` (409 si ya existe uno).

### Documents — `/api/documents`
CRUD completo. Un `Student` puede tener múltiples `Document`.

## Formato de respuesta paginada

```json
{
  "content": [],
  "pageNumber": 0,
  "pageSize": 10,
  "totalElements": 100,
  "totalPages": 10
}
```

## Formato de error

```json
{
  "timestamp": "2026-09-14T15:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Student with id 12 was not found",
  "path": "/api/students/12"
}
```

| Status | Cuándo |
|---|---|
| 400 | Datos inválidos (validaciones `@NotBlank`, `@Email`, `@Size`, etc.) |
| 404 | Recurso no encontrado |
| 409 | Conflicto (email/código duplicado, o estudiante que ya tiene contacto) |
| 500 | Error inesperado |

## Reglas de negocio

- El `email` de `Student` es único.
- `programId` debe existir dentro del módulo.
- Un `Student` puede tener un solo `Contact`.
- Un `Student` puede tener múltiples `Document`.
- El `code` de `Program` es único.
- No hay foreign keys hacia otros módulos (Enrollments, Subjects) — la relación entre servicios se resuelve por HTTP, no por base de datos.

## Ejecutar las pruebas

```bash
./mvnw test
```

Cobertura:
- **Pruebas unitarias de servicio** (Mockito): CRUD, "not found" y "duplicate" para `Student`, `Program`, `Contact` y `Document`.
- **Pruebas de controlador** (`@WebMvcTest` + `MockMvc`): status codes, paginación/filtros/orden, y el formato del error para cada endpoint.

## Estructura del proyecto

```
src/main/java/co/edu/uptc/students/
├── controller/   # Controladores REST
├── service/      # Lógica de negocio
├── repository/   # Acceso a datos (Spring Data JPA)
├── entity/       # Entidades JPA
├── dto/          # Request/Response, desacoplados de las entidades
└── exception/    # Excepciones y manejador global de errores
```

## Responsable

Módulo `students-service` — Karen Cruz.