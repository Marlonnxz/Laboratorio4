# Sistema Académico Distribuido — Laboratorio 4
**UPTC Sogamoso — Escuela de Ingeniería de Sistemas y Computación**
*Base de datos, caché y almacenamiento de archivos en producción (Microsoft Azure)*

---

## 1. Arquitectura del Sistema

El sistema implementa una arquitectura basada en microservicios independientes, desacoplados y empaquetados en contenedores Docker, orquestados en la nube a través de **Azure Container Apps (ACA)**:

1. **`api-gateway` (Spring Cloud Gateway WebFlux, :8080)**: Punto único de entrada al backend. Valida tokens JWT, aplica reglas de autorización por rol (RBAC), implementa caché en memoria con Redis (*Cache-Aside* con cabecera `X-Cache: HIT/MISS`), provee degradación elegante ante caídas y orquesta el endpoint de composición `GET /api/students/{id}/detail`.
2. **`auth-service` (Spring Boot 3, :8084)**: Microservicio de autenticación independiente con base de datos propia (`auth_db`). Contraseñas cifradas con BCrypt y emisión de tokens JWT con roles (`ADMIN`, `ESTUDIANTE`).
3. **`students-service` (Spring Boot 3, :8081)**: Gestión de estudiantes y carga de fotografías ($\le$ 2MB, JPG/PNG) en contenedor privado de **Azure Blob Storage**, generando URLs temporales firmadas (SAS Tokens). Base de datos `students_db`.
4. **`subjects-service` (Spring Boot 3, :8082)**: Catálogo de materias y contenidos temáticos en PDF ($\le$ 10MB) en **Azure Blob Storage**, con metadatos en `subjects_db` y URLs firmadas SAS.
5. **`enrollments-service` (Spring Boot 3, :8083)**: Gestión transaccional de matrículas, historial de estados y calificaciones con base de datos `enrollments_db`.
6. **`frontend` (React + Vite + Tailwind CSS + Nginx, :3000 / :80)**: SPA moderna que consume exclusivamente el API Gateway, con telemetría en tiempo real de `X-Cache` y herramienta integrada de benchmark.

---

## 2. Despliegue Oficial en Microsoft Azure (Cloud)

El sistema se encuentra desplegado y activo en la nube de Microsoft Azure bajo la suscripción Azure for Students:

| Servicio | URL Pública Oficial (HTTPS) |
| :--- | :--- |
| **Portal Web (Frontend)** | [https://frontend.whitegrass-404c6009.northcentralus.azurecontainerapps.io](https://frontend.whitegrass-404c6009.northcentralus.azurecontainerapps.io) |
| **API Gateway** | [https://api-gateway.whitegrass-404c6009.northcentralus.azurecontainerapps.io](https://api-gateway.whitegrass-404c6009.northcentralus.azurecontainerapps.io) |
| **Health Check (Actuator)** | [https://api-gateway.whitegrass-404c6009.northcentralus.azurecontainerapps.io/actuator/health](https://api-gateway.whitegrass-404c6009.northcentralus.azurecontainerapps.io/actuator/health) |
| **Swagger UI** | [https://api-gateway.whitegrass-404c6009.northcentralus.azurecontainerapps.io/swagger-ui.html](https://api-gateway.whitegrass-404c6009.northcentralus.azurecontainerapps.io/swagger-ui.html) |

---

## 3. Requisitos Previos (Entorno Local)

- **Docker** y **Docker Compose** instalados (probado en Docker 29+).
- Navegador web moderno (Chrome, Edge, Firefox).

---

## 3. Puesta en Marcha en Entorno Local

1. Clonar el repositorio o ubicarse en la carpeta raíz del proyecto:
   ```bash
   cd Laboratorio4
   ```

2. (Opcional) Copiar variables de entorno:
   ```bash
   cp .env.example .env
   ```

3. Construir e iniciar todos los servicios con Docker Compose:
   ```bash
   docker compose up --build -d
   ```

4. Verificar que todos los contenedores estén saludables:
   ```bash
   docker compose ps
   ```

5. Abrir en el navegador:
   - **Frontend Web**: [http://localhost:3000](http://localhost:3000)
   - **API Gateway Swagger / Actuator**: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)
   - **Swagger UI del Gateway**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

---

## 4. Credenciales de Prueba Preconfiguradas

| Rol | Usuario / Correo | Contraseña | Capacidades y Reglas |
| :--- | :--- | :--- | :--- |
| **ADMIN** | `admin@uptc.edu.co` | `Admin123*` | Control total: crea materias, sube PDFs/fotos, consulta cualquier estudiante en `/detail`. |
| **ESTUDIANTE** | `estudiante1@uptc.edu.co` | `Student123*` | Restringido: solo consulta su propio `/detail` (ID 1). Intentar crear materias o ver otro ID genera **HTTP 403 Forbidden**. |
| **ESTUDIANTE 2** | `estudiante2@uptc.edu.co` | `Student123*` | Restringido al ID 2 para pruebas cruzadas de autorización. |

---

## 5. Pruebas y Comportamientos Requeridos en Laboratorio 4

### A. Caché Redis (Patrón Cache-Aside en Gateway)
- **Primera consulta a materias (`GET /api/subjects`)**: Retorna cabecera `X-Cache: MISS` (obtenido del microservicio downstream y guardado en Redis con TTL de 180s).
- **Segunda consulta idéntica (`GET /api/subjects`)**: Retorna cabecera `X-Cache: HIT` con latencia significativamente menor.
- **Invalidación**: Al ejecutar `POST`, `PUT` o `DELETE` sobre `/api/subjects`, el Gateway incrementa atómicamente la versión del caché en Redis, invalidando al instante todas las consultas de catálogo.
- **Resiliencia ante fallo de Redis**: Si Redis se detiene (`docker compose stop redis`), el Gateway captura la excepción y sirve la petición directamente desde `subjects-service` sin arrojar error 500, marcando `X-Cache: MISS` o `BYPASS`.

### B. Almacenamiento de Archivos (Azure Blob Storage)
- **Bucket Privado**: El contenedor no permite acceso público anónimo.
- **URLs temporales firmadas (SAS Tokens)**: El acceso a fotos y PDFs se realiza con firmas criptográficas de lectura que vencen en 15 minutos.
- **Acceso directo sin firma**: Retorna **HTTP 403 Forbidden**.

### C. Seguridad y Roles
- **Petición sin token**: Retorna **HTTP 401 Unauthorized**.
- **Estudiante modificando materia o viendo otro perfil**: Retorna **HTTP 403 Forbidden**.

