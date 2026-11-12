# 🛡️ API de Gestión de Pólizas de Arrendamiento

> **Prueba técnica · Desarrollador TI / Sénior**

API REST para la administración de **pólizas de arrendamiento** (individuales y colectivas), sus **riesgos asociados** y la integración resiliente con un sistema **CORE asegurador legado (WebLogic)**.

El proyecto aplica **Spec-Driven Development (SDD)**: cada módulo parte de su especificación, luego el diseño y finalmente la implementación. Está construido sobre una arquitectura **Hexagonal + Event-Driven + CQRS**, con **observabilidad**, **cobertura de código**, **integración continua** e **infraestructura como código**.

---

## 📑 Tabla de Contenidos

1. [Descripción General](#-descripción-general)
2. [Arquitectura](#-arquitectura)
3. [Estructura del Proyecto](#-estructura-del-proyecto)
4. [Reglas de Negocio](#-reglas-de-negocio)
5. [Requisitos Previos](#-requisitos-previos)
6. [Compilar, Probar y Ejecutar](#-compilar-probar-y-ejecutar)
7. [Catálogo de Endpoints](#-catálogo-de-endpoints)
8. [Datos Precargados](#-datos-precargados)
9. [Observabilidad](#-observabilidad)
10. [Calidad, CI/CD e Infraestructura](#-calidad-cicd-e-infraestructura)

---

## 📖 Descripción General

La aplicación expone un conjunto de endpoints REST para gestionar el ciclo de vida completo de una póliza de arrendamiento:

- **Crear** pólizas individuales y colectivas.
- **Renovar** pólizas aplicando el ajuste de canon por **IPC** (regulado por la Ley 820 de 2003 en Colombia).
- **Cancelar** pólizas con efecto cascada sobre sus riesgos.
- **Agregar / cancelar** riesgos asegurables.
- **Sincronizar** cada cambio de estado con el sistema CORE legado mediante eventos.

Toda la lógica de negocio vive aislada en el **dominio**, lo que permite testearla sin base de datos ni frameworks, y reemplazar cualquier tecnología externa (BD, mensajería, legado) sin tocar el núcleo.

---

## 🏗️ Arquitectura

El proyecto implementa una combinación de patrones complementarios:

### 1. Hexagonal (Ports & Adapters)

El **dominio** es el corazón de la aplicación y **no depende de Spring ni de JPA**. Todo lo externo (REST, persistencia, mensajería, el legado) se conecta a través de **puertos** (interfaces) e **implementaciones** (adaptadores).

```
       ┌──────────────────────────────┐
       │          DOMINIO             │  ← entidades, reglas, eventos
       │   (sin Spring, sin JPA)      │     y puertos (interfaces)
       └──────────────┬───────────────┘
           ▲          │          ▲
   (puertos de entrada)│  (puertos de salida)
           │          ▼          │
   ┌───────────────┐   ┌──────────────────────────────┐
   │  APPLICATION  │   │      INFRASTRUCTURE          │
   │  (casos de uso│   │ REST · Persistencia · Eventos│
   │   CQRS)       │   │ CORE legado · Configuración  │
   └───────────────┘   └──────────────────────────────┘
```

**Dónde vive cada cosa:**

| Capa | Responsabilidad | Modelos y Clases (Inglés) |
|------|-----------------|---------------------------|
| `domain` | Entidades puras, invariantes de negocio, eventos, puertos de salida. | `Policy`, `Risk`, `PolicyRenewedEvent`, `PolicyRepository` |
| `application` | Orquestación de casos de uso (CQRS Commands & Queries). | `PolicyCommandService`, `PolicyQueryService`, `RiskCommandService` |
| `infrastructure` | Adaptadores REST, persistencia JPA, integración ACL hacia CORE legado. | `PolicyController`, `RiskController`, `PolicyJpaEntity`, `CoreIntegrationAdapter` |

### 2. Event-Driven Architecture (EDA)

Las acciones sobre el dominio **no acoplan** servicios de forma síncrona. Cada mutación relevante publica un **evento de dominio inmutable** que se consume de forma desacoplada:

| Evento | Cuándo se emite |
|--------|-----------------|
| `PolicyCreatedEvent` | Al registrar una nueva póliza. |
| `PolicyRenewedEvent` | Al renovar (lleva canon anterior/nuevo e IPC). |
| `PolicyCancelledEvent` | Al cancelar una póliza. |
| `RiskAddedEvent` | Al incluir un riesgo en una colectiva. |
| `RiskCancelledEvent` | Al cancelar un riesgo individual. |

El agregado `Policy` acumula los eventos (`drainEvents()`), y estos se publican **después del commit** de la transacción (`@TransactionalEventListener(AFTER_COMMIT)`) a través del `CoreEventRelay`, que los reenvía al CORE. Si el legado falla, **no revierte la transacción principal** (en producción iría a una cola de reintentos / DLQ).

### 3. CQRS (Command Query Responsibility Segregation)

Separación explícita entre lo que **cambia estado** y lo que **consulta**:

- **Lado de escritura (Command):** `PolicyCommandService` y `RiskCommandService` implementan los `CommandHandler`. Reciben **comandos inmutables** (`RenewPolicyCommand`, `CancelPolicyCommand`, `AddRiskCommand`, …).
- **Lado de lectura (Query):** `PolicyQueryService` implementa `PolicyQuery` con transacciones de solo lectura.

Esta separación permite escalar, cachear o proyectar (read-model) cada lado de forma independiente.

### 4. Anti-Corruption Layer (ACL)

`CoreIntegrationAdapter` traduce los eventos del dominio al **contrato del sistema legado WebLogic**, protegiendo al núcleo de la "corrupción semántica" del sistema externo. Si mañana el CORE cambia de SOAP a REST, solo se modifica este adaptador.

### 5. Seguridad Perimetral

El filtro `ApiKeyFilter` exige el header `x-api-key: 123456` en todas las rutas de negocio. Las rutas de diagnóstico (`/h2-console`, `/actuator`, Swagger) quedan excluidas.

---

## 🏛️ Estructura del Proyecto

```
polizas-app/
├── .github/workflows/ci.yml          # Integración continua (GitHub Actions)
├── terraform/                        # Infraestructura como código (AWS ECS Fargate)
├── Dockerfile                        # Imagen multi-etapa (Maven → JRE)
├── pom.xml                           # Dependencias, JaCoCo y plugins
├── mvnw.bat                          # Wrapper Maven con JDK 21
├── SPEC.md                           # Especificación técnica (SDD)
├── README.md                         # Este documento
└── src/
    ├── main/
    │   ├── java/com/seguros/polizas/
    │   │   ├── PolizasApiApplication.java
    │   │   │
    │   │   ├── domain/                           # NÚCLEO (hexagonal)
    │   │   │   ├── model/                        # Poliza (aggregate), Riesgo (entidad)
    │   │   │   ├── enums/                        # TipoPoliza, EstadoPoliza, EstadoRiesgo
    │   │   │   ├── event/                        # Eventos de dominio + EventPublisher
    │   │   │   ├── exception/                    # Excepciones de dominio
    │   │   │   └── port/out/                     # Puertos de salida (repos, publisher)
    │   │   │
    │   │   ├── application/                      # Casos de uso (CQRS)
    │   │   │   ├── command/                      # Comandos (write model)
    │   │   │   ├── port/in/                      # Puertos de entrada (handlers, queries)
    │   │   │   └── service/                      # Implementación de casos de uso
    │   │   │
    │   │   └── infrastructure/                   # Adaptadores (entrada/salida)
    │   │       ├── web/rest/                     # Controllers + DTOs (adapter in)
    │   │       ├── persistence/                  # Entidades JPA + repos (adapter out)
    │   │       ├── event/                        # SpringEventPublisher + CoreEventRelay
    │   │       ├── integration/                  # ACL hacia CORE legado
    │   │       └── config/                       # Filtros, RestTemplate, Seguridad, OpenAPI
    │   └── resources/
    │       ├── application.yml                   # Configuración (BD, puertos, seguridad)
    │       └── data.sql                          # Semilla de datos iniciales
    └── test/
        └── java/com/seguros/polizas/             # Tests organizados por capa
            ├── domain/model/                     # Tests de dominio puro
            ├── application/service/              # Tests de casos de uso
            ├── infrastructure/persistence/       # Tests @DataJpaTest
            ├── infrastructure/web/rest/          # Tests @WebMvcTest
            ├── infrastructure/event/             # Tests del relay de eventos
            ├── infrastructure/config/            # Tests de filtros
            └── integration/                      # Tests end-to-end
```

---

## 📋 Reglas de Negocio

| # | Regla | Comportamiento | Código HTTP |
|---|-------|----------------|-------------|
| 1 | **Póliza individual** | Solo permite **1 riesgo**. Agregar más riesgos a una individual es rechazado. | `422` |
| 2 | **Póliza colectiva** | Soporta múltiples riesgos (agregar/cancelar libremente). | `201` / `200` |
| 3 | **Renovación por IPC** | `canonNuevo = canonActual × (1 + IPC/100)`; la prima se recalcula (`canon × meses`); el estado pasa a `RENOVADA` y se extiende la vigencia. | `200` |
| 4 | **Póliza cancelada** | No se puede renovar una póliza en estado `CANCELADA`. | `422` |
| 5 | **Cancelación en cascada** | Cancelar una póliza pasa a `CANCELADA` y **todos** sus riesgos a `CANCELADO`. | `200` |
| 6 | **Sincronización con CORE** | Cada mutación publica un evento de dominio que el `CoreEventRelay` reenvía al CORE legado (mock). | `200` (mock) |
| 7 | **Seguridad por API Key** | Header `x-api-key: 123456` obligatorio en todas las peticiones de negocio. | `401` |

---

## ✅ Requisitos Previos

- **Java 21** (el wrapper `mvnw.bat` ya apunta a un JDK 21 si está en `C:\Program Files\Java\jdk-21.0.10`).
- **Maven** (opcional: el wrapper lo descarga automáticamente si está disponible en la caché local).
- Para **Terraform** (opcional): Terraform ≥ 1.5 y credenciales de AWS.
- Para **Docker** (opcional): Docker Desktop o compatible.

---

## 🚀 Compilar, Probar y Ejecutar

> Todos los comandos se ejecutan desde la raíz del proyecto.

### 6.1. Compilar y ejecutar los tests

```powershell
.\mvnw.bat clean test
```

### 6.2. Generar reporte de cobertura (JaCoCo)

```powershell
.\mvnw.bat clean verify
```

- Aplica un **gate de cobertura** mínimo (60% instrucciones, 50% branches). Si baja de ese umbral, el build falla.
- El reporte HTML se genera en `target/site/jacoco/index.html`.

### 6.3. Levantar la aplicación

```powershell
.\mvnw.bat spring-boot:run
```

La aplicación inicia en `http://localhost:8080`.

### 6.4. Documentación interactiva (Swagger UI)

Con la app encendida:

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8080/v3/api-docs

> 💡 **Autenticarse en Swagger:** haz clic en **Authorize**, escribe `123456` en el campo `Value` y confirma. Las pruebas con **Try it out** incluirán el header `x-api-key` automáticamente.

### 6.5. Consola H2

- **URL:** http://localhost:8080/h2-console
- **JDBC URL:** `jdbc:h2:mem:polizasdb`
- **User:** `sa` · **Password:** *(vacío)*

---

## 📡 Catálogo de Endpoints

> Todos los endpoints requieren el header `x-api-key: 123456`.
> Prefijo base: `/api/v1`

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/api/v1/polizas` | Listar pólizas (filtros opcionales `tipo`, `estado`). |
| `GET` | `/api/v1/polizas/{id}` | Detalle de una póliza. |
| `GET` | `/api/v1/polizas/{id}/riesgos` | Riesgos de una póliza. |
| `POST` | `/api/v1/polizas` | Crear póliza. |
| `POST` | `/api/v1/polizas/{id}/renovar` | Renovar aplicando IPC. |
| `POST` | `/api/v1/polizas/{id}/cancelar` | Cancelar (cascada). |
| `POST` | `/api/v1/polizas/{id}/riesgos` | Agregar riesgo (solo colectiva). |
| `POST` | `/api/v1/riesgos/{id}/cancelar` | Cancelar riesgo individual. |
| `POST` | `/api/v1/core-mock/evento` | Mock del CORE legado. |

### Ejemplos `curl`

```bash
# Listar pólizas (filtrando)
curl -X GET "http://localhost:8080/api/v1/polizas?tipo=INDIVIDUAL&estado=VIGENTE" \
     -H "x-api-key: 123456"

# Renovar con IPC del 5.10%
curl -X POST "http://localhost:8080/api/v1/polizas/1/renovar" \
     -H "x-api-key: 123456" \
     -H "Content-Type: application/json" \
     -d '{"ipc": 5.10}'

# Cancelar una póliza (cascada)
curl -X POST "http://localhost:8080/api/v1/polizas/2/cancelar" \
     -H "x-api-key: 123456"

# Agregar riesgo a una colectiva
curl -X POST "http://localhost:8080/api/v1/polizas/2/riesgos" \
     -H "x-api-key: 123456" \
     -H "Content-Type: application/json" \
     -d '{
        "direccionInmueble": "Carrera 15 # 85-30 Oficina 501",
        "ciudad": "Bogota",
        "nombreAsegurado": "Soluciones Digitales SAS",
        "valorAsegurado": 3800000.00
     }'

# Mock del CORE
curl -X POST "http://localhost:8080/api/v1/core-mock/evento" \
     -H "x-api-key: 123456" \
     -H "Content-Type: application/json" \
     -d '{"evento": "ACTUALIZACION", "polizaId": 555}'
```

> También puedes usar la colección [`polizas.http`](polizas.http) (REST Client / IntelliJ) o el script [`probar-api.ps1`](probar-api.ps1).

---

## 📊 Datos Precargados

La semilla `data.sql` carga 6 pólizas para probar todos los escenarios:

| ID | Número | Tipo | Estado | Canon | Riesgos |
|----|--------|------|--------|-------|---------|
| 1 | `POL-IND-001` | Individual | VIGENTE | $1.500.000 | 1 activo |
| 2 | `POL-COL-002` | Colectiva | VIGENTE | $4.500.000 | 2 activos |
| 3 | `POL-IND-003` | Individual | CANCELADA | $1.200.000 | 1 cancelado |
| 4 | `POL-COL-004` | Colectiva | RENOVADA | $6.200.000 | 2 activos |
| 5 | `POL-IND-005` | Individual | VIGENTE (24m) | $3.800.000 | 1 activo |
| 6 | `POL-COL-006` | Colectiva | CANCELADA | $8.500.000 | 2 cancelados |

---

## 🔍 Observabilidad

La aplicación ya incluye observabilidad lista para usar:

- **Actuator:** `/actuator/health`, `/actuator/info`, `/actuator/metrics` (health con `show-details: always`).
- **Trazabilidad:** el filtro `CorrelationIdFilter` genera/hereda un `X-Correlation-Id` por petición y lo inyecta en el **MDC** de los logs (`[corrId=...]`).
- **Logging estructurado:** niveles DEBUG para el dominio y SQL, con contexto de correlación.
- **Healthcheck del contenedor:** el `Dockerfile` usa `/actuator/health` como `HEALTHCHECK`.

Para pasar de este nivel a **ELK / Prometheus + Grafana / Jaeger**, el proyecto ya está preparado: métricas vía Micrometer y logs con correlation id listos para exportar.

---

## 🧪 Calidad, CI/CD e Infraestructura

### 9.1. Análisis de cobertura (JaCoCo)

Configurado en `pom.xml` con:

- Generación del reporte en `target/site/jacoco/`.
- **Gate de calidad**: mínimo **60% de instrucciones** y **50% de branches**. El build `verify` falla si se incumple.

```powershell
.\mvnw.bat clean verify
```

La suite actual consta de **59 tests** distribuidos por la pirámide de pruebas (dominio, aplicación, persistencia, web, eventos e integración).

### 9.2. Integración continua (GitHub Actions)

El workflow `.github/workflows/ci.yml` ejecuta en cada push y pull request:

1. **Build + test + coverage** (`mvn clean verify`) sobre Java 21.
2. **Subida del reporte** JaCoCo como artifact.
3. **Comentario de cobertura** automático en el PR.
4. **Validación de la imagen Docker** (solo en `main`).

### 9.3. Docker

```powershell
# Construir la imagen
docker build -t polizas-app .

# Ejecutar
docker run --rm -p 8080:8080 polizas-app
```

La imagen es **multi-etapa**: compila con Maven + JDK 21 y ejecuta con un JRE Alpine mínimo (usuario no-root + healthcheck).

### 9.4. Infraestructura como código (Terraform)

La carpeta [`terraform/`](terraform/) define en AWS un **ECS Fargate** listo para desplegar:

- **VPC + subnets + Internet Gateway**.
- **ECR** (repositorio de imágenes).
- **ECS** (cluster, task definition y servicio Fargate con healthcheck).
- **CloudWatch Logs** para centralizar logs.
- **Roles IAM** de ejecución y tarea.

```bash
cd terraform
terraform init
terraform plan
terraform apply
```

Consulta [`terraform/README.md`](terraform/README.md) para más detalles.

---

## 🧭 Flujo de una Operación (de punta a punta)

Para ilustrar cómo se conectan las capas, esto es lo que ocurre al **renovar una póliza**:

```
HTTP POST /api/v1/polizas/1/renovar
        │
        ▼
PolizaController  (infrastructure/web/rest)     ← adapter de entrada
        │  traduce a RenovarPolizaCommand
        ▼
PolizaCommandService (application/service)      ← caso de uso (CQRS command)
        │  carga el agregado vía puerto PolizaRepository
        ▼
Poliza.renovar(ipc)   (domain/model)            ← regla de negocio pura
        │  calcula canon/prima, registra PolizaRenovada
        ▼
PolizaRepositoryAdapter → JPA (infrastructure/persistence)  ← persiste
        │
        ▼
SpringEventPublisher (infrastructure/event)     ← publica el evento
        │  (después del commit)
        ▼
CoreEventRelay (infrastructure/event)           ← consume el evento
        │
        ▼
CoreIntegrationAdapter (ACL)                    ← reenvía al CORE legado
```

Este flujo muestra la **separación de responsabilidades**: la lógica de negocio no conoce HTTP, SQL ni al legado.
