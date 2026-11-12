# 📝 Bitácora de Implementación — Suite Completa de Pruebas Unitarias y de Integración
**Proyecto:** API de Gestión de Pólizas de Arrendamiento
**Fecha:** Noviembre 2026
**Framework de Pruebas:** JUnit 5 (Jupiter), Mockito, Spring Boot Test (`MockMvc`), `@DataJpaTest`, `@WebMvcTest`.
**Resultado Global:** **44 tests ejecutados | 0 fallos | 0 errores | 100% éxito**.

---

## 🏗️ Pirámide de Pruebas Implementada

```
                        ▲
                       / \
                      /E2E\      -> PolizaLifecycleIntegrationTest (Flujo completo multi-paso)
                     /-----\
                    / Inte- \    -> PolizasApiApplicationTests (MockMvc + Spring Context + Security)
                   / gración \   -> PolizaRepositoryTest, RiesgoRepositoryTest (@DataJpaTest)
                  /-----------\
                 / Controlador \ -> PolizaControllerTest, RiesgoControllerTest, CoreMockControllerTest (@WebMvcTest)
                /   Unitarios   \-> PolizaServiceTest, RiesgoServiceTest (Mockito puro)
               /     Dominio     \-> PolizaTest (Entidad rica, reglas de cálculo, IPC, invariantes)
              /   Seguridad Pura  \-> ApiKeyFilterTest (MockHttpServletRequest / Response)
             /─────────────────────\
```

---

## 📊 Matriz Detallada de Pruebas (44 Tests)

### 1. Capa de Dominio (Pure Unit Tests - Sin Framework)
* **Archivo:** [`PolizaTest.java`](file:///C:/Users/Juan%20Rodriguez/.gemini/antigravity/scratch/polizas-app/src/test/java/com/seguros/polizas/model/entity/PolizaTest.java)
* **Objetivo:** Garantizar que la lógica de negocio y las invariantes del modelo no dependan de frameworks.
* **Casos Cubiertos (8 tests):**
  1. `testCalcularPrima`: Verificación de `canon * meses` con escala decimal y redondeo `HALF_UP`.
  2. `testCalcularPrimaValoresNulos`: Retorno seguro de `BigDecimal.ZERO` ante parámetros nulos.
  3. `testRenovarAplicaFormulaIpc` (Prueba parametrizada con 4 variantes de IPC y canon):
     * Variante 1: Canon \$1.000.000, IPC 5.00% -> Canon \$1.050.000, Prima \$12.600.000.
     * Variante 2: Canon \$2.000.000, IPC 5.10% -> Canon \$2.102.000, Prima \$25.224.000.
     * Variante 3: Canon \$1.500.000, IPC 10.00% -> Canon \$1.650.000, Prima \$19.800.000.
     * Variante 4: Canon \$800.000, IPC 3.25% -> Canon \$826.000, Prima \$9.912.000.
     * Verificación del desplazamiento cronológico del periodo de vigencia (+12 meses).
  4. `testRenovarPolizaCanceladaLanzaExcepcion`: Invariante de entidad que impide renovar en estado `CANCELADA` lanzando `IllegalStateException`.
  5. `testCancelarCascadaRiesgos`: Mutación en cascada de todos los riesgos a estado `CANCELADO`.
  6. `testAddRiesgoRelacionBidireccional`: Integridad de asociación bidireccional entre `Poliza` y `Riesgo`.

---

### 2. Capa de Seguridad (Unit Tests de Filtro)
* **Archivo:** [`ApiKeyFilterTest.java`](file:///C:/Users/Juan%20Rodriguez/.gemini/antigravity/scratch/polizas-app/src/test/java/com/seguros/polizas/config/ApiKeyFilterTest.java)
* **Objetivo:** Verificar la barrera de autenticación perimetral.
* **Casos Cubiertos (4 tests):**
  1. Petición sin header `x-api-key` -> HTTP 401 Unauthorized y cuerpo JSON descriptivo.
  2. Petición con valor incorrecto -> HTTP 401 Unauthorized.
  3. Petición con clave válida `123456` -> Pasa el filtro y continúa la ejecución (HTTP 200).
  4. Rutas excluidas (`shouldNotFilter`) como `/h2-console` se omiten adecuadamente.

---

### 3. Capa de Servicios (Unit Tests con Mocks)
* **Archivos:**
  * [`PolizaServiceTest.java`](file:///C:/Users/Juan%20Rodriguez/.gemini/antigravity/scratch/polizas-app/src/test/java/com/seguros/polizas/service/PolizaServiceTest.java)
  * [`RiesgoServiceTest.java`](file:///C:/Users/Juan%20Rodriguez/.gemini/antigravity/scratch/polizas-app/src/test/java/com/seguros/polizas/service/RiesgoServiceTest.java)
* **Objetivo:** Comprobar la orquestación de negocio y las interacciones con repositorios y CORE.
* **Casos Cubiertos (7 tests):**
  1. `testRenovarPolizaVigenteExitosamente`: Renovación + notificación `RENOVACION` al servicio CORE.
  2. `testRenovarPolizaCanceladaLanzaExcepcion`: Lanza `BusinessRuleException` y nunca persiste ni notifica al CORE.
  3. `testCancelarPolizaCancelaTodosLosRiesgos`: Cancela póliza y riesgos + notificación `CANCELACION_POLIZA`.
  4. `testCrearPolizaConNumeroDuplicadoFalla`: Validación de unicidad de negocio.
  5. `testAgregarRiesgoAPolizaColectivaExitoso`: Inclusión permitida en colectiva + notificación `INCLUSION_RIESGO`.
  6. `testAgregarRiesgoAPolizaIndividualLanzaExcepcion`: **Regla de oro de negocio:** Póliza individual limitada a 1 riesgo (rechazo con `BusinessRuleException`).
  7. `testCancelarRiesgoIndividual`: Transición a `CANCELADO` de un riesgo específico + notificación `CANCELACION_RIESGO`.

---

### 4. Capa Web / Controladores REST (`@WebMvcTest`)
* **Archivos:**
  * [`PolizaControllerTest.java`](file:///C:/Users/Juan%20Rodriguez/.gemini/antigravity/scratch/polizas-app/src/test/java/com/seguros/polizas/controller/PolizaControllerTest.java)
  * [`RiesgoControllerTest.java`](file:///C:/Users/Juan%20Rodriguez/.gemini/antigravity/scratch/polizas-app/src/test/java/com/seguros/polizas/controller/RiesgoControllerTest.java)
  * [`CoreMockControllerTest.java`](file:///C:/Users/Juan%20Rodriguez/.gemini/antigravity/scratch/polizas-app/src/test/java/com/seguros/polizas/controller/CoreMockControllerTest.java)
* **Objetivo:** Verificar serialización JSON, validación de beans (`@Valid`), status codes HTTP y manejo global de excepciones.
* **Casos Cubiertos (10 tests):**
  1. `GET /api/v1/polizas`: Status 200 y serialización DTO correcta.
  2. `GET /api/v1/polizas/{id}`: Status 404 ante `PolizaNotFoundException`.
  3. `POST /api/v1/polizas`: Status 400 Bad Request ante payload inválido (campos vacíos).
  4. `POST /api/v1/polizas/{id}/renovar`: Status 400 Bad Request ante IPC negativo o nulo.
  5. `POST /api/v1/polizas/{id}/renovar`: Status 422 ante `BusinessRuleException` (póliza cancelada).
  6. `POST /api/v1/polizas/{id}/cancelar`: Status 200 y respuesta DTO actualizada.
  7. `POST /api/v1/riesgos/{id}/cancelar`: Status 200.
  8. `POST /api/v1/riesgos/{id}/cancelar`: Status 404 si riesgo no existe.
  9. `POST /api/v1/core-mock/evento`: Status 200 y registro de auditoría en mock de WebLogic.
  10. `POST /api/v1/core-mock/evento`: Status 400 ante body con campos en blanco.

---

### 5. Capa de Persistencia (`@DataJpaTest`)
* **Archivos:**
  * [`PolizaRepositoryTest.java`](file:///C:/Users/Juan%20Rodriguez/.gemini/antigravity/scratch/polizas-app/src/test/java/com/seguros/polizas/repository/PolizaRepositoryTest.java)
  * [`RiesgoRepositoryTest.java`](file:///C:/Users/Juan%20Rodriguez/.gemini/antigravity/scratch/polizas-app/src/test/java/com/seguros/polizas/repository/RiesgoRepositoryTest.java)
* **Objetivo:** Probar queries derivadas de Spring Data y restricciones relacionales sobre BD H2 real.
* **Casos Cubiertos (4 tests):**
  1. `findByTipoAndEstado`: Filtrado relacional compuesto.
  2. `existsByNumeroPoliza`: Comprobación de existencia.
  3. `testRestriccionUnicaNumeroPoliza`: Violación de constraint `UNIQUE` en base de datos.
  4. `findByPolizaId`: Filtrado de riesgos hijos por clave foránea.

---

### 6. Pruebas de Integración y End-to-End (`@SpringBootTest` + `MockMvc`)
* **Archivos:**
  * [`PolizasApiApplicationTests.java`](file:///C:/Users/Juan%20Rodriguez/.gemini/antigravity/scratch/polizas-app/src/test/java/com/seguros/polizas/PolizasApiApplicationTests.java) (9 tests)
  * [`PolizaLifecycleIntegrationTest.java`](file:///C:/Users/Juan%20Rodriguez/.gemini/antigravity/scratch/polizas-app/src/test/java/com/seguros/polizas/integration/PolizaLifecycleIntegrationTest.java) (1 test de ciclo de vida completo)
* **Objetivo:** Validar el sistema completo con contexto de Spring levantado y base de datos en memoria.
* **Casos Cubiertos (10 tests):**
  1. Seguridad perimetral sin clave -> 401.
  2. Seguridad perimetral con clave -> 200.
  3. Listado con filtros combinados en base de datos.
  4. Listado de riesgos precargados.
  5. Renovación end-to-end con cálculo matemático de IPC.
  6. Rechazo de renovación sobre póliza cancelada (422).
  7. Rechazo de inclusión de riesgo en póliza individual (422).
  8. Inclusión exitosa de riesgo en póliza colectiva (201).
  9. Mock CORE funcional (200).
  10. **Escenario E2E Completo:**
      * Creación de colectiva (\$5M canon, \$60M prima).
      * Alta de 2 riesgos independientes.
      * Cancelación de 1 riesgo (queda 1 ACTIVO, 1 CANCELADO).
      * Renovación con IPC 5.10% (\$5.255.000 canon, \$63.060.000 prima).
      * Cancelación de la póliza matriz.
      * Verificación de que todos los riesgos pasaron a CANCELADO en BD.
      * Intento de renovación post-cancelación -> rechazo 422.

---

## ⚡ Ejecución de la Suite Completa

```powershell
cd "C:\Users\Juan Rodriguez\.gemini\antigravity\scratch\polizas-app"
.\mvnw.bat test
```

Salida de Maven Surefire:
```
[INFO] Results:
[INFO] 
[INFO] Tests run: 44, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  12.273 s
```
