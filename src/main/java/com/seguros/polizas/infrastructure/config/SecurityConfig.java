package com.seguros.polizas.infrastructure.config;

import org.springframework.context.annotation.Configuration;

/**
 * Configuración central de seguridad perimetral.
 *
 * <p>La verificación del header {@code x-api-key} se realiza en el
 * {@link ApiKeyFilter} (OncePerRequestFilter) que se registra automáticamente
 * como {@code @Component}. Esta clase actúa como punto único de configuración
 * documentando las rutas públicas excluidas del control de acceso:</p>
 *
 * <ul>
 *     <li>{@code /h2-console/**} — consola de base de datos H2 (desarrollo)</li>
 *     <li>{@code /actuator/**} — endpoints de observabilidad / health checks</li>
 *     <li>{@code /swagger-ui/**}, {@code /v3/api-docs/**} — documentación OpenAPI</li>
 * </ul>
 *
 * <p>Toda ruta de negocio bajo {@code /api/v1/**} exige el header
 * {@code x-api-key: 123456}; en caso contrario se responde HTTP 401.</p>
 */
@Configuration
public class SecurityConfig {
}
