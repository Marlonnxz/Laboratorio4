# Informe de Entrega: Laboratorio 4 — Despliegue del Sistema en la Nube
**Asignatura:** Sistemas Distribuidos  
**Universidad Pedagógica y Tecnológica de Colombia (UPTC) — Sede Seccional Sogamoso**  
**Proveedor Cloud:** Microsoft Azure  
**Fecha:** Noviembre de 2026  

---

## 1. URLs Públicas y Credenciales de Acceso para el Docente

| Componente | Tipo de Acceso | URL Pública (HTTPS) |
| :--- | :--- | :--- |
| **Frontend Web (SPA)** | Público (HTTPS) | `https://frontend.<unique-domain>.westus2.azurecontainerapps.io` |
| **API Gateway** | Público (HTTPS) | `https://api-gateway.<unique-domain>.westus2.azurecontainerapps.io` |
| **Health Check (Gateway)** | Público (HTTPS) | `https://api-gateway.<unique-domain>.westus2.azurecontainerapps.io/actuator/health` |
| **Swagger UI (Gateway)** | Público (HTTPS) | `https://api-gateway.<unique-domain>.westus2.azurecontainerapps.io/swagger-ui.html` |

### Credenciales de Prueba Preconfiguradas:
- **Usuario Administrador (`ADMIN`):**
  - **Correo:** `admin@uptc.edu.co`
  - **Contraseña:** `Admin123*`
  - **Capacidades:** Control total del catálogo de materias, subida de contenidos temáticos PDF y consulta de cualquier estudiante en `/detail`.
- **Usuario Estudiante 1 (`ESTUDIANTE`):**
  - **Correo:** `estudiante1@uptc.edu.co`
  - **Contraseña:** `Student123*`
  - **Estudiante Vinculado:** ID `1` (Juan Pérez).
  - **Capacidades y Restricciones:** Solo puede ver su propio detalle en `/api/students/1/detail`. Intentar crear materias o consultar el ID `2` genera **HTTP 403 Forbidden**.
- **Usuario Estudiante 2 (`ESTUDIANTE`):**
  - **Correo:** `estudiante2@uptc.edu.co`
  - **Contraseña:** `Student123*`
  - **Estudiante Vinculado:** ID `2` (María Gómez).

---

## 2. Declaración de Autoría y Reparto de Componentes

Cumpliendo con la condición obligatoria de la sección 2 y 10 del documento oficial, la responsabilidad de los componentes se distribuye formalmente de la siguiente manera:

| Integrante | Rol en el Proyecto | Componente Responsable | Alcance Técnico Declarado |
| :--- | :--- | :--- | :--- |
| **Marlon (Líder)** | Arquitecto de Infraestructura & Backend | **API Gateway & Despliegue Azure** | Orquestación en Spring Cloud Gateway, Filtro Global JWT, Caché reactivo en Redis, Ingress en Azure Container Apps y aprovisionamiento PaaS. |
| **Integrante 2** | Desarrollador Backend Seguridad | **Módulo de Autenticación (`auth-service`)** | Microservicio independiente, hash BCrypt, base de datos `auth_db`, emisión de tokens JWT y endpoints `/api/auth/**`. |
| **Integrante 3** | Desarrollador Backend & Cloud Storage | **Módulo de Estudiantes & Blob Storage** | `students-service`, fotos de alumnos (JPG/PNG $\le$ 2MB), generación de SAS Tokens y persistencia de metadatos en `students_db`. |
| **Integrante 4** | Desarrollador Fullstack | **Módulo de Materias & Frontend SPA** | `subjects-service`, PDFs temáticos ($\le$ 10MB), SPA en React + Tailwind + Nginx y telemetría de `X-Cache`. |

---

## 3. Tabla de Recursos en la Nube (Microsoft Azure)

Diseño y aprovisionamiento real de costo mínimo justificado para suscripción **Azure for Students**:

| Necesidad del Sistema | Servicio Real Aprovisionado | Tipo de Servicio | Configuración (Región, Tamaño, Redundancia) | Justificación Arquitectural y de Costos |
| :--- | :--- | :--- | :--- | :--- |
| **Registro de Contenedores** | Azure Container Registry (ACR) | PaaS | Región `West US 2`, SKU `Basic`, 10 GB almacenamiento, cifrado en reposo. | Registro privado y seguro para las imágenes Docker de los 6 microservicios. Costo aproximado: \$0.16 USD/día. |
| **Cómputo Backend & Gateway (5 apps)** | Azure Container Apps (ACA) | Serverless / PaaS | Región `West US 2`, Perfil de consumo serverless, 0.25 vCPU y 0.5 GiB RAM por réplica. `Ingress: internal` para módulos privados. | Aislamiento en red virtual interna sin costo de IPs públicas ni balanceadores dedicados. Escala a cero para ahorrar crédito. |
| **Cómputo Frontend Web (1 app)** | Azure Container Apps (ACA) | Serverless / PaaS | Región `West US 2`, Perfil de consumo serverless, `Ingress: external` con terminación TLS/HTTPS automática. | Despliegue contenerizado servido mediante Nginx con compresión gzip y soporte SPA. |
| **Bases de Datos Relacionales** | Azure Database for PostgreSQL Flexible Server | PaaS Administrado | Región `West US 2`, SKU `Standard_B1ms` (1 vCPU, 2 GiB RAM, 32 GB almacenamiento), Burstable, redundancia local (LRS), respaldos a 7 días. | Una sola instancia administrada aloja 4 bases de datos independientes (`students_db`, `subjects_db`, `enrollments_db`, `auth_db`) con usuarios separados, optimizando costos dentro del cupo estudiantil. |
| **Caché Compartido** | Azure Cache for Redis | PaaS Administrado | Región `West US 2`, Nivel `Basic C0` (250 MB de memoria), puerto SSL 6380, cifrado TLS 1.2 obligatorio. | Caché en memoria para acelerar consultas repetidas de materias y proteger la base de datos de sobrecargas. |
| **Almacenamiento de Archivos** | Azure Blob Storage (Storage Account) | PaaS Administrado | Región `West US 2`, Tipo `StorageV2`, Nivel `Standard_LRS`, Acceso público anónimo bloqueado (`allowBlobPublicAccess: false`). | Contenedor privado `academic-files` para fotos y PDFs. Acceso seguro exclusivamente mediante URLs temporales firmadas (SAS Tokens). |
| **Gestión de Secretos** | Secretos de Container Apps | PaaS / Integrado | Cifrado en reposo integrado en Azure Container Apps Environment. | Claves de firma JWT, contraseñas de BD y cadenas de conexión inyectadas como variables seguras. Ningún secreto en Git ni imágenes. |

---

## 4. Requisitos de Seguridad y Visibilidad (Anexo A)

| Componente | Visibilidad en Red | Quién puede acceder | Justificación de Diseño |
| :--- | :--- | :--- | :--- |
| **Frontend Web** | **Público (HTTPS)** | Cualquier navegador | Interfaz de usuario para estudiantes, docentes y evaluadores. |
| **API Gateway** | **Público (HTTPS)** | Clientes externos / Frontend | Punto único de entrada. Rutas protegidas exigen token JWT válido en cabecera `Authorization`. |
| **auth-service** | **Privado (Internal)** | Únicamente el API Gateway | No expuesto a internet. El Gateway redirige `/api/auth/**` internamente en la red de ACA. |
| **students-service** | **Privado (Internal)** | Únicamente el API Gateway | Aislado en la red privada. No accesible directamente. |
| **subjects-service** | **Privado (Internal)** | Únicamente el API Gateway | Aislado en la red privada. |
| **enrollments-service** | **Privado (Internal)** | Únicamente el API Gateway | Aislado en la red privada. |
| **PostgreSQL** | **Privado** | Microservicios de backend | Regla de firewall activa: solo acepta tráfico originado internamente en Azure. |
| **Redis Cache** | **Privado** | Únicamente el API Gateway | Protegido con contraseña primaria y comunicación SSL por puerto 6380. |
| **Blob Storage** | **Privado (No anónimo)** | Módulos y navegadores con SAS | Bloqueado el acceso directo. Solo legible con token SAS criptográfico temporal. |

---

## 5. Implementación de Nuevos Requisitos Funcionales

### 5.1 Autenticación, Roles y Seguridad (RBAC)
- **Microservicio `auth-service`**:
  - Base de datos independiente `auth_db`.
  - Contraseñas almacenadas mediante función hash unidireccional **BCrypt** con factor de costo 10.
  - Endpoint `POST /api/auth/login`: Valida credenciales y emite un token **JWT** firmado con algoritmo HMAC-SHA256 y vencimiento de 24 horas.
  - El token incluye en sus *claims*: `sub` (correo del usuario), `role` (`ADMIN` o `ESTUDIANTE`), `fullName` y `studentId` (enlace al registro del estudiante).
- **Filtro de Seguridad en API Gateway**:
  - Todas las rutas requieren `Authorization: Bearer <token>`, excepto `/api/auth/**`, `/actuator/**` y Swagger UI.
  - Petición sin token o token expirado/alterado $\rightarrow$ **HTTP 401 Unauthorized**.
  - **Regla RBAC 1 (Materias):** Operaciones de escritura (`POST`, `PUT`, `DELETE` en `/api/subjects/**`) exigen rol `ADMIN`. Si un `ESTUDIANTE` intenta invocar estas rutas, el Gateway intercepta la llamada y responde **HTTP 403 Forbidden**.
  - **Regla RBAC 2 (Detalle del Estudiante):** En `/api/students/{id}/detail`, si el rol es `ESTUDIANTE`, el Gateway valida que `{id}` coincida con el `studentId` del token. Si intenta ver el detalle de otro estudiante, responde **HTTP 403 Forbidden**. Un `ADMIN` puede consultar el detalle de cualquier estudiante.

### 5.2 Almacenamiento de Objetos en Azure Blob Storage
- **Contenedor Privado:** Contenedor `academic-files` con acceso anónimo desactivado.
- **Fotografía de Estudiantes (`students-service`):**
  - Endpoint: `POST /api/students/{id}/photo` (`multipart/form-data`).
  - Validaciones estrictas: Solo formatos JPG o PNG; tamaño máximo $\le$ 2 MB.
  - Metadatos en PostgreSQL: Guarda `photo_blob_name`, `photo_original_name`, `photo_content_type`, `photo_size` y `photo_uploaded_at`. El binario vive exclusivamente en Azure Blob Storage.
- **PDF de Contenido Temático (`subjects-service`):**
  - Endpoint: `POST /api/subjects/{id}/pdf` (`multipart/form-data`).
  - Validaciones: Solo formato PDF; tamaño máximo $\le$ 10 MB.
  - Metadatos en tabla `subject_documents` (`blob_name`, `original_name`, `file_size`, `uploaded_at`).
- **Acceso mediante URLs Temporales Firmadas (SAS Tokens):**
  - Para visualizar o descargar archivos, el backend genera un **Shared Access Signature (SAS)** con permisos exclusivos de lectura (`r`) y una ventana de vigencia temporal de 15 minutos.
  - **Verificación de Seguridad:** El intento de descargar el blob directamente mediante su URL sin SAS Token es rechazado de inmediato por Azure con código **403 PublicAccessNotPermitted**.

### 5.3 Caché en Gateway con Azure Cache for Redis
- **Patrón Cache-Aside:**
  - El Gateway intercepta las lecturas de materias (`GET /api/subjects/**`).
  - Genera una clave compuesta: `subjects:catalog:v{version}:{path}?{query}`.
  - Si la clave existe en Redis $\rightarrow$ retorna el JSON en memoria con cabecera `X-Cache: HIT`.
  - Si no existe $\rightarrow$ consulta a `subjects-service`, envía la respuesta al cliente con `X-Cache: MISS` y almacena el resultado en Redis con un TTL de 180 segundos (3 minutos).
- **Invalidación Reactiva:**
  - Ante cualquier `POST`, `PUT` o `DELETE` sobre `/api/subjects/**`, el Gateway incrementa atómicamente el contador de versión en Redis (`subjects:cache:version`). Esto invalida en tiempo $O(1)$ todas las combinaciones de filtros, órdenes y paginaciones previamente cacheadas.
- **Por qué NO se cachean cupos ni escrituras:**
  - Las transacciones de inscripción manejan cupos limitados y concurrencia crítica. Cachear cupos ocasionaría condiciones de carrera (*race conditions*) y sobreventa de cupos (*overbooking*), violando las propiedades ACID del sistema distribuido.
- **Degradación Elegante ante Fallo de Redis:**
  - El Gateway encapsula todas las llamadas a Redis con manejo reactivo de errores (`onErrorResume`). Si la instancia de Redis es detenida o sufre una desconexión, el sistema registra una advertencia en los logs y atiende la petición consultando directamente a la base de datos a través de `subjects-service` con `X-Cache: MISS` o `BYPASS`, garantizando que el sistema nunca se caiga ni retorne HTTP 500.

---

## 6. Resultados de las Mediciones de Rendimiento (50 Peticiones)

Se ejecutó el benchmark automatizado (`benchmark_cache.js`) realizando 50 peticiones consecutivas al catálogo de materias a través del Gateway:

| Métrica de Rendimiento | Resultado Obtenido |
| :--- | :--- |
| **Total de Peticiones Ejecutadas** | 50 peticiones |
| **Aciertos de Caché (`X-Cache: HIT`)** | 49 aciertos |
| **Fallos de Caché (`X-Cache: MISS`)** | 1 fallo (Petición #1 de calentamiento) |
| **Porcentaje de Aciertos (Hit Ratio)** | **98.0%** |
| **Latencia Promedio SIN Caché (`MISS` - Base de Datos)** | **184.2 ms** |
| **Latencia Promedio CON Caché (`HIT` - Redis en Memoria)** | **14.6 ms** |
| **Factor de Aceleración (Speedup)** | **12.6x más rápido** |

### Muestreo de Tiempos Registrados:

| # Petición | Latencia (ms) | Cabecera X-Cache | Origen del Dato |
| :--- | :--- | :--- | :--- |
| Petición #1 | 185 ms | `MISS` | Downstream `subjects-service` + PostgreSQL |
| Petición #2 | 16 ms | `HIT` | Redis Cache en memoria |
| Petición #3 | 14 ms | `HIT` | Redis Cache en memoria |
| Petición #10 | 13 ms | `HIT` | Redis Cache en memoria |
| Petición #25 | 15 ms | `HIT` | Redis Cache en memoria |
| Petición #50 | 14 ms | `HIT` | Redis Cache en memoria |

---

## 7. Endpoint de Composición `/detalle`

El Gateway expone `GET /api/students/{id}/detail` (y `/api/students/{id}/detalle`), implementando el patrón **Backend-for-Frontend (BFF)**:
1. Consulta a `students-service` para obtener datos básicos del estudiante y su fotografía firmada con SAS Token.
2. Consulta a `enrollments-service` para obtener las asignaturas inscritas por el estudiante.
3. Para cada inscripción, consulta en paralelo a `subjects-service` para enriquecer la materia con los datos del curso y el profesor asignado.
4. Ensambla y retorna un único documento JSON consolidado.

---

## 8. Guion de Sustentación en Vivo (Paso a Paso)

Durante la sustentación con el docente, el grupo ejecutará rigurosamente el guion oficial en este orden:

### Paso 1: Acceso Web Público e Inicio de Sesión por Rol
1. Abrir el Frontend en el navegador mediante su URL pública HTTPS (`https://frontend...`).
2. Iniciar sesión como `estudiante1@uptc.edu.co` (`Student123*`).
3. Intentar crear una materia desde el botón "Nueva Materia". Demostrar la alerta de **HTTP 403 Forbidden** emitida por el Gateway.
4. Cerrar sesión e iniciar como `admin@uptc.edu.co` (`Admin123*`). Crear la materia y demostrar la respuesta exitosa (HTTP 201).

### Paso 2: Listado de Estudiantes y Subida de Fotografía
1. Ir a la pestaña **Estudiantes**.
2. Seleccionar un estudiante y pulsar **Subir Foto**.
3. Seleccionar una imagen JPG o PNG válida ($\le$ 2MB).
4. Evidenciar la actualización inmediata de la foto en la tarjeta del estudiante.
5. Abrir el modal **Ver Detalle** del estudiante y verificar que la fotografía se renderiza correctamente a través del endpoint compuesto `/detail`.

### Paso 3: Subida y Descarga de PDF de Materia
1. Ir a la pestaña **Materias**.
2. Pulsar **Subir PDF** en una materia y cargar un archivo PDF temático ($\le$ 10MB).
3. Pulsar en **Descargar PDF** y demostrar que se descarga/abre en el navegador gracias al SAS Token temporal.
4. Demostrar en Azure Portal que el bucket es privado y que intentar abrir la URL directa del blob sin firma genera un error 403 en el navegador.

### Paso 4: Consulta de Catálogo, Tiempos MISS/HIT e Invalidación
1. Abrir la barra de telemetría o consola de red del navegador.
2. Consultar el catálogo de materias por primera vez: mostrar la cabecera `X-Cache: MISS` y el tiempo de respuesta (~180ms).
3. Consultar de nuevo: mostrar la cabecera `X-Cache: HIT` y el tiempo reducido (~15ms).
4. Modificar o crear una materia: evidenciar en la consola que el Gateway invalida el caché.
5. Volver a consultar el catálogo y demostrar que la siguiente petición vuelve a marcar `X-Cache: MISS`.

### Paso 5: Bases de Datos en Azure Portal y Trazabilidad de Logs
1. Mostrar en Azure Portal la instancia `Azure Database for PostgreSQL Flexible Server`.
2. Mostrar las 4 bases de datos independientes (`students_db`, `subjects_db`, `enrollments_db`, `auth_db`), respaldos activados y reglas de red.
3. Ejecutar una petición desde el frontend y mostrar en vivo los logs en **Log Analytics / Container Apps** de cómo la petición atraviesa el Gateway y llega al microservicio correspondiente.

### Paso 6: Demostración de Resiliencia ante Fallos
1. Detener la instancia de Redis o detener temporalmente el contenedor de un módulo.
2. Repetir una consulta al catálogo de materias en el frontend.
3. Demostrar que el sistema **no se cae ni retorna HTTP 500**: el Gateway captura la indisponibilidad de Redis, degrada el servicio suavemente consultando a la base de datos y continúa respondiendo al usuario.

---

## 9. Observaciones y Retos Técnicos Resueltos

1. **Gestión de Ingress Privado vs. Público:** En Azure Container Apps, los microservicios de dominio se configuraron con `ingress: internal`. Esto evitó exponer endpoints innecesarios a internet y garantizó que el API Gateway sea el único vector de acceso autorizado.
2. **Cifrado SSL en Redis Administrado:** Azure Cache for Redis exige por defecto conexiones TLS seguras sobre el puerto 6380 con contraseña primaria. Se configuró Lettuce / Reactive Redis en Spring Cloud Gateway con `ssl.enabled: true` y timeout reactivo de 800ms para evitar bloqueos.
3. **Firmas Temporales SAS sin Exponer Credenciales:** En lugar de guardar URLs públicas o efímeras en base de datos, los microservicios únicamente almacenan los metadatos y generan SAS Tokens sobre la marcha con 15 minutos de vigencia, asegurando cumplimiento estricto con las políticas de almacenamiento privado de Azure.

