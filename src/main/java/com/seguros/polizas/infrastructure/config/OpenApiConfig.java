package com.seguros.polizas.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "ApiKeyAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("🛡️ API de Gestión de Pólizas de Arrendamiento")
                        .version("1.0.0")
                        .description("Plataforma RESTful para la administración de pólizas individuales y colectivas de arrendamiento, " +
                                     "gestión de riesgos asociados, renovación con ajuste de IPC (Ley 820) e integración con el CORE asegurador legado en WebLogic.")
                        .contact(new Contact()
                                .name("Equipo de Arquitectura e Ingeniería TI")
                                .email("soporte-ti@seguros.com")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name("x-api-key")
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.HEADER)
                                        .description("Clave de acceso requerida para autorizar las solicitudes. Valor por defecto: **123456**")));
    }
}

