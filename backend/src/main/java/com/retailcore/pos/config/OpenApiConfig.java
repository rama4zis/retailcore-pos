package com.retailcore.pos.config;

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

    public static final String BEARER_AUTH = "bearerAuth";

    @Bean
    OpenAPI retailCoreOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("RetailCore POS API")
                        .version("v1")
                        .description("Backend API for RetailCore POS covering authentication, users, categories, "
                                + "products, inventory, sales, receipts, payments, refunds, and reports.")
                        .contact(new Contact().name("RetailCore POS")))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Use the JWT returned by POST /api/auth/login.")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));
    }
}
