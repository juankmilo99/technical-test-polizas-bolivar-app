# SPEC: API de Gestión de Pólizas (Módulo 2)
**Metodología:** Spec-Driven Development (SDD)
**Rol:** Desarrollador TI / Sénior
**Stack:** Java 21, Spring Boot 3.3.5, Spring Data JPA, H2 Database, Bean Validation, JUnit 5, Mockito.

---

## 1. Alcance y Objetivos
Construir una API REST robusta, resiliente y testeable para la administración de pólizas de arrendamiento (individuales y colectivas) y sus riesgos asociados, con sincronización de eventos hacia el sistema CORE legado y seguridad por API Key.

---

## 2. Requisitos Funcionales y Contratos API

### Seguridad Global
* **Mecanismo:** Header obligatorio `x-api-key`.
* **Valor válido:** `123456`.
* **Comportamiento si falta o es inválido:** HTTP `401 Unauthorized`.
* **Excepción:** El endpoint mock `/api/v1/core-mock/evento` puede o no requerirla (para desacoplamiento de pruebas, se valida que soporte llamadas internas/externas).

### Endpoints Requeridos

| Método | Endpoint | Descripción | Parámetros / Body | Códigos HTTP |
|---|---|---|---|---|
| `GET` | `/api/v1/polizas` | Listar pólizas con filtro opcional | Query Params: `tipo` (INDIVIDUAL, COLECTIVA), `estado` (VIGENTE, RENOVADA, CANCELADA) | `200 OK` |
| `GET` | `/api/v1/polizas/{id}` | Consultar detalle de póliza | Path: `id` (Long) | `200 OK`, `404 Not Found` |
| `GET` | `/api/v1/polizas/{id}/riesgos` | Listar riesgos de una póliza | Path: `id` (Long) | `200 OK`, `404 Not Found` |
| `POST` | `/api/v1/polizas` | Crear una póliza inicial | Body: `PolizaRequest` | `201 Created`, `400 Bad Request` |
| `POST` | `/api/v1/polizas/{id}/renovar` | Renovar póliza aplicando IPC | Body: `RenovacionRequest` (`{ "ipc": 5.10 }`) | `200 OK`, `404 Not Found`, `422 Unprocessable` |
| `POST` | `/api/v1/polizas/{id}/cancelar` | Cancelar póliza y sus riesgos | Path: `id` (Long) | `200 OK`, `404 Not Found` |
| `POST` | `/api/v1/polizas/{id}/riesgos` | Agregar riesgo a póliza colectiva | Path: `id` (Long), Body: `RiesgoRequest` | `201 Created`, `404 Not Found`, `422 Unprocessable` |
| `POST` | `/api/v1/riesgos/{id}/cancelar`| Cancelar un riesgo específico | Path: `id` (Long) | `200 OK`, `404 Not Found` |
| `POST` | `/api/v1/core-mock/evento` | Mock de integración con WebLogic/CORE | Body: `CoreEventoRequest` (`{ "evento": "...", "polizaId": 555 }`) | `200 OK` |

---

## 3. Reglas de Negocio Esenciales

1. **Invariante de Póliza Individual:**
   * Una póliza individual solo puede tener como máximo 1 riesgo.
   * `POST /api/v1/polizas/{id}/riesgos` debe fallar con HTTP `422 Unprocessable Entity` si la póliza es de tipo `INDIVIDUAL`.

2. **Restricción de Renovación:**
   * No se puede renovar una póliza que se encuentre en estado `CANCELADA`. Retorna HTTP `422 Unprocessable Entity`.
   * Si la póliza está `VIGENTE` o `RENOVADA`:
     * Incrementa `canonMensual = canonMensual * (1 + ipc / 100)`.
     * Recalcula `primaTotal = canonMensual * mesesVigencia`.
     * Estado pasa a `RENOVADA`.
     * Se extiende el periodo de vigencia.

3. **Cancelación en Cascada:**
   * Al cancelar una póliza (`POST /api/v1/polizas/{id}/cancelar`), su estado pasa a `CANCELADA` y **todos sus riesgos asociados pasan a `CANCELADO`**.

4. **Sincronización con el CORE Legado:**
   * Toda acción que modifique estado de pólizas o riesgos (`renovar`, `cancelar`, `agregar riesgo`, `cancelar riesgo`) debe disparar una invocación al servicio de edición del CORE (simulado mediante `POST /api/v1/core-mock/evento`).
   * La operación se registra en logs con trazabilidad (evento y polizaId).

---

## 4. Criterios de Aceptación y Pruebas
1. Suite de pruebas unitarias (`PolizaServiceTest`, `RiesgoServiceTest`) con cobertura de todas las reglas de negocio.
2. Suite de pruebas de integración (`PolizasApiApplicationTests`) verificando contratos HTTP, seguridad por `x-api-key` y código mock.
3. Compilación limpia y ejecución de tests con `mvn clean test`.

