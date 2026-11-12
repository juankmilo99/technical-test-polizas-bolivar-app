# 🛡️ API de Gestión de Pólizas de Arrendamiento (Prueba Sénior TI)

API REST desarrollada bajo la metodología **Spec-Driven Development (SDD)**, orientada a la administración de pólizas individuales y colectivas de arrendamiento, gestión de riesgos asociados, integración resiliente con el sistema CORE asegurador legado en WebLogic y seguridad perimetral por API Key.

---

## 🏗️ 1. Arquitectura y Stack Tecnológico

* **Lenguaje:** Java 21 (LTS)
* **Framework:** Spring Boot 3.3.5
* **Persistencia:** Spring Data JPA + Hibernate ORM
* **Base de Datos:** H2 Database (In-Memory, con consola web `/h2-console`)
* **Validación:** Jakarta Bean Validation (Hibernate Validator)
* **Testing:** JUnit 5, Mockito, Spring Boot Test (MockMvc)
* **Patrones de Arquitectura Implementados:**
  * **Hexagonal Architecture (Ports & Adapters):** Núcleo de dominio puro (sin dependencias de Spring/JPA), separado de los adaptadores de entrada (REST) y salida (persistencia JPA, eventos, CORE legado) mediante puertos.
  * **Event-Driven Architecture (EDA):** Eventos de dominio inmutables (`PolizaCreada`, `PolizaRenovada`, `PolizaCancelada`, etc.) publicados por el agregado y consumidos de forma asíncrona tras el commit de la transacción (`CoreEventRelay`).
  * **CQRS (Command Query Responsibility Segregation):** Separación explícita entre el lado de escritura (comandos + `CommandHandler`) y el lado de lectura (queries + `QueryService`).
  * **Anti-Corruption Layer (ACL):** `CoreIntegrationAdapter` traduce los eventos del dominio al contrato del sistema WebLogic legado, aislando el núcleo de la corrupción semántica.
  * **Filter Security Pattern:** Intercepción temprana (`ApiKeyFilter`) para verificación obligatoria del header `x-api-key`.

---

## 🏛️ 2. Estructura del Proyecto

```
polizas-app/
├── pom.xml                                 # Dependencias y configuración Maven
├── mvnw.bat                                # Wrapper Maven preconfigurado con JDK 21
├── SPEC.md                                 # Especificación técnica formal (SDD)
├── README.md                               # Documentación integral
└── src/
    ├── main/
    │   ├── java/com/seguros/polizas/
    │   │   ├── PolizasApiApplication.java   # Clase principal Spring Boot
    │   │   ├── domain/                      # NÚCLEO (hexagonal): sin Spring/JPA
    │   │   │   ├── model/                   # Poliza (aggregate), Riesgo (entidad)
    │   │   │   ├── enums/                   # TipoPoliza, EstadoPoliza, EstadoRiesgo
    │   │   │   ├── event/                   # Domain events + EventPublisher (puerto)
    │   │   │   ├── exception/               # Excepciones de dominio
    │   │   │   └── port/out/                # Puertos de salida (PolizaRepository, RiesgoRepository)
    │   │   ├── application/                 # Casos de uso (CQRS)
    │   │   │   ├── command/                 # Comandos (write model)
    │   │   │   ├── query/                   # (read model implícito en PolizaQuery)
    │   │   │   ├── port/in/                 # Puertos de entrada (CommandHandler, Query)
    │   │   │   └── service/                 # Implementación de casos de uso
    │   │   └── infrastructure/              # Adaptadores (entrada/salida)
    │   │       ├── web/rest/                # Controllers + DTOs (adapter in)
    │   │       ├── persistence/             # Entidades JPA + repos (adapter out)
    │   │       ├── event/                   # SpringEventPublisher + CoreEventRelay
    │   │       ├── integration/             # ACL hacia el CORE legado (adapter out)
    │   │       └── config/                  # Filtros, RestTemplate, Seguridad, OpenAPI
    │   └── resources/
    │       ├── application.yml             # Configuración de BD, puertos y seguridad
    │       └── data.sql                    # Semilla de datos iniciales
    └── test/
        └── java/com/seguros/polizas/        # Tests por capa (domain, application,
                                             # infrastructure/persistence, web/rest, integration)
```
            ├── application/service/        # Tests de casos de uso (CommandHandler)
            ├── domain/model/               # Tests de dominio (entidad rica)
            ├── infrastructure/persistence/ # Tests de persistencia (@DataJpaTest)
            ├── infrastructure/web/rest/    # Tests de capa web (@WebMvcTest)
            └── integration/                # Tests end-to-end de ciclo de vida
```

---

## 📋 3. Reglas de Negocio Implementadas

| Regla | Descripción | Código de Error |
|---|---|---|
| **Riesgo en Póliza Individual** | Una póliza individual solo puede tener **1 riesgo**. Intentar agregar más riesgos a una póliza individual rechaza la operación. | `422 Unprocessable Entity` |
| **Póliza Colectiva** | Una póliza colectiva soporta múltiples riesgos y permite agregar (`POST /polizas/{id}/riesgos`) y cancelar (`POST /riesgos/{id}/cancelar`). | `201 Created` / `200 OK` |
| **Renovación con IPC** | Al renovar (`POST /polizas/{id}/renovar`), se incrementa el canon según la tasa de IPC enviada (`canonNuevo = canonActual * (1 + ipc/100)`), se recalcula la prima (`canon * meses`), se actualiza la vigencia y el estado pasa a `RENOVADA`. | `200 OK` |
| **Póliza Cancelada no Renovable** | No se permite renovar una póliza que se encuentre en estado `CANCELADA`. | `422 Unprocessable Entity` |
| **Cancelación en Cascada** | La cancelación de una póliza (`POST /polizas/{id}/cancelar`) cambia su estado a `CANCELADA` y automáticamente cancela **todos sus riesgos asociados**. | `200 OK` |
| **Notificación Obligatoria al CORE** | Toda acción que modifique estados dispara una invocación al endpoint agnóstico `POST /core-mock/evento`, dejando trazabilidad en logs del intento de envío. | `200 OK` en mock |
| **Seguridad por Header** | Toda solicitud debe incluir el header `x-api-key: 123456`. Solicitudes sin él o con valor incorrecto son rechazadas inmediatamente. | `401 Unauthorized` |

---

## 🚀 4. Guía Rápida de Compilación y Ejecución

### 4.1. Ejecutar la Suite Completa de Pruebas (48 Tests)
Abre una terminal PowerShell en la raíz del proyecto y ejecuta:
```powershell
.\mvnw.bat test
```

#### Cobertura de la Suite (Pirámide de Pruebas):
* **Dominio Puro (9 tests):** [`PolizaTest`](file:///C:/Users/Juan%20Rodriguez/.gemini/antigravity/scratch/polizas-app/src/test/java/com/seguros/polizas/domain/model/PolizaTest.java) con pruebas parametrizadas para fórmulas de IPC, redondeo `HALF_UP`, invariantes de póliza cancelada y cancelaciones en cascada.
* **Seguridad Perimetral (4 tests):** [`ApiKeyFilterTest`](file:///C:/Users/Juan%20Rodriguez/.gemini/antigravity/scratch/polizas-app/src/test/java/com/seguros/polizas/infrastructure/config/ApiKeyFilterTest.java) validando peticiones no autorizadas (401), claves válidas (200) y exclusión de consola H2.
* **Capa de Aplicación (6 tests):** [`PolizaCommandServiceTest`](file:///C:/Users/Juan%20Rodriguez/.gemini/antigravity/scratch/polizas-app/src/test/java/com/seguros/polizas/application/service/PolizaCommandServiceTest.java) y [`RiesgoCommandServiceTest`](file:///C:/Users/Juan%20Rodriguez/.gemini/antigravity/scratch/polizas-app/src/test/java/com/seguros/polizas/application/service/RiesgoCommandServiceTest.java) con Mockito, validando reglas de negocio y publicación de eventos de dominio.
* **Capa Web (10 tests):** [`PolizaControllerTest`](file:///C:/Users/Juan%20Rodriguez/.gemini/antigravity/scratch/polizas-app/src/test/java/com/seguros/polizas/infrastructure/web/rest/PolizaControllerTest.java), [`RiesgoControllerTest`](file:///C:/Users/Juan%20Rodriguez/.gemini/antigravity/scratch/polizas-app/src/test/java/com/seguros/polizas/infrastructure/web/rest/RiesgoControllerTest.java), y [`CoreMockControllerTest`](file:///C:/Users/Juan%20Rodriguez/.gemini/antigravity/scratch/polizas-app/src/test/java/com/seguros/polizas/infrastructure/web/rest/CoreMockControllerTest.java) usando `@WebMvcTest`.
* **Capa de Persistencia (4 tests):** [`PolizaJpaRepositoryTest`](file:///C:/Users/Juan%20Rodriguez/.gemini/antigravity/scratch/polizas-app/src/test/java/com/seguros/polizas/infrastructure/persistence/PolizaJpaRepositoryTest.java) y [`RiesgoJpaRepositoryTest`](file:///C:/Users/Juan%20Rodriguez/.gemini/antigravity/scratch/polizas-app/src/test/java/com/seguros/polizas/infrastructure/persistence/RiesgoJpaRepositoryTest.java) con `@DataJpaTest` y H2 real.
* **Integración End-to-End (13 tests):** [`PolizasApiApplicationTests`](file:///C:/Users/Juan%20Rodriguez/.gemini/antigravity/scratch/polizas-app/src/test/java/com/seguros/polizas/PolizasApiApplicationTests.java) y [`PolizaLifecycleIntegrationTest`](file:///C:/Users/Juan%20Rodriguez/.gemini/antigravity/scratch/polizas-app/src/test/java/com/seguros/polizas/integration/PolizaLifecycleIntegrationTest.java) validando el ciclo de vida completo.

### 4.2. Iniciar la Aplicación
```powershell
.\mvnw.bat spring-boot:run
```
La aplicación iniciará en `http://localhost:8080`.

### 4.3. Documentación Interactiva Swagger UI (OpenAPI 3)
Con la aplicación encendida, puedes explorar y ejecutar todos los endpoints interactivamente desde tu navegador:
* **Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
* **OpenAPI Docs (JSON):** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

> **💡 Cómo autenticarse en Swagger UI:**
> 1. Haz clic en el botón verde con el candado **"Authorize"** en la parte superior derecha de la pantalla.
> 2. En el campo `Value`, ingresa: `123456`.
> 3. Haz clic en **Authorize** y luego **Close**.
> 4. ¡Listo! Todas las solicitudes que pruebes con el botón **"Try it out"** incluirán automáticamente el header `x-api-key: 123456`.

### 4.4. Consola H2 Database
Puedes inspeccionar la base de datos en tiempo real accediendo desde tu navegador a:
* **URL:** [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
* **JDBC URL:** `jdbc:h2:mem:polizasdb`
* **User:** `sa`
* **Password:** *(dejar vacío)*

---

## 📡 5. Catálogo de Endpoints y Ejemplos cURL

> **Nota:** Todos los comandos incluyen el header requerido `-H "x-api-key: 123456"`.

### 1. Listar Pólizas (con filtros opcionales de tipo y estado)
```bash
# Listar todas
curl -X GET "http://localhost:8080/api/v1/polizas" \
     -H "x-api-key: 123456"

# Filtrar por tipo y estado
curl -X GET "http://localhost:8080/api/v1/polizas?tipo=INDIVIDUAL&estado=VIGENTE" \
     -H "x-api-key: 123456"
```

### 2. Consultar Riesgos de una Póliza
```bash
curl -X GET "http://localhost:8080/api/v1/polizas/1/riesgos" \
     -H "x-api-key: 123456"
```

### 3. Renovar Póliza aplicando IPC
```bash
curl -X POST "http://localhost:8080/api/v1/polizas/1/renovar" \
     -H "x-api-key: 123456" \
     -H "Content-Type: application/json" \
     -d '{"ipc": 5.10}'
```

### 4. Cancelar Póliza (Cascada a todos sus riesgos)
```bash
curl -X POST "http://localhost:8080/api/v1/polizas/2/cancelar" \
     -H "x-api-key: 123456"
```

### 5. Agregar Riesgo a Póliza Colectiva
```bash
curl -X POST "http://localhost:8080/api/v1/polizas/2/riesgos" \
     -H "x-api-key: 123456" \
     -H "Content-Type: application/json" \
     -d '{
       "direccionInmueble": "Carrera 15 # 85-30 Oficina 501",
       "ciudad": "Bogota",
       "nombreAsegurado": "Soluciones Digitales SAS",
       "valorAsegurado": 3800000.00
     }'
```

### 6. Cancelar un Riesgo Individual
```bash
curl -X POST "http://localhost:8080/api/v1/riesgos/2/cancelar" \
     -H "x-api-key: 123456"
```

### 7. Endpoint Mock de Integración con el CORE
```bash
curl -X POST "http://localhost:8080/api/v1/core-mock/evento" \
     -H "x-api-key: 123456" \
     -H "Content-Type: application/json" \
     -d '{
       "evento": "ACTUALIZACION",
       "polizaId": 555
     }'
```

---

## 📊 6. Datos Precargados para Pruebas (`data.sql`)

* **Póliza ID 1:** `POL-IND-001` (Tipo: `INDIVIDUAL`, Estado: `VIGENTE`, Canon: \$1.500.000, 1 riesgo activo).
* **Póliza ID 2:** `POL-COL-002` (Tipo: `COLECTIVA`, Estado: `VIGENTE`, Canon: \$4.500.000, 2 riesgos activos).
* **Póliza ID 3:** `POL-IND-003` (Tipo: `INDIVIDUAL`, Estado: `CANCELADA`, para validar rechazo de renovación con 422).
* **Póliza ID 4:** `POL-COL-004` (Tipo: `COLECTIVA`, Estado: `RENOVADA`, Canon: \$6.200.000, 2 riesgos activos en Edificio Titanium).
* **Póliza ID 5:** `POL-IND-005` (Tipo: `INDIVIDUAL`, Estado: `VIGENTE`, Vigencia: 24 meses, Canon: \$3.800.000, 1 riesgo activo).
* **Póliza ID 6:** `POL-COL-006` (Tipo: `COLECTIVA`, Estado: `CANCELADA`, Canon: \$8.500.000, 2 riesgos cancelados de locales comerciales).

