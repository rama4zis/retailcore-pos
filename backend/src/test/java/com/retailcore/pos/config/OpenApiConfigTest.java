package com.retailcore.pos.config;

import static org.assertj.core.api.Assertions.assertThat;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.Test;

class OpenApiConfigTest {

    @Test
    void openApiMetadataDocumentsRetailCoreApi() {
        OpenAPI openAPI = new OpenApiConfig().retailCoreOpenApi();

        assertThat(openAPI.getInfo().getTitle()).isEqualTo("RetailCore POS API");
        assertThat(openAPI.getInfo().getVersion()).isEqualTo("v1");
        assertThat(openAPI.getInfo().getDescription())
                .contains("product", "inventory", "sales", "payments", "refunds", "reports");
    }

    @Test
    void openApiDefinesJwtBearerSecurityScheme() {
        OpenAPI openAPI = new OpenApiConfig().retailCoreOpenApi();

        assertThat(openAPI.getComponents().getSecuritySchemes())
                .containsKey("bearerAuth");
        SecurityScheme scheme = openAPI.getComponents().getSecuritySchemes().get("bearerAuth");
        assertThat(scheme.getType()).isEqualTo(SecurityScheme.Type.HTTP);
        assertThat(scheme.getScheme()).isEqualTo("bearer");
        assertThat(scheme.getBearerFormat()).isEqualTo("JWT");
        assertThat(openAPI.getSecurity()).hasSize(1);
    }
}
