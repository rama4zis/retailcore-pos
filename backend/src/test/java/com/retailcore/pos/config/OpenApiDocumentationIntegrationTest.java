package com.retailcore.pos.config;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiDocumentationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void apiDocsArePublicAndExposeMainModulesAndJwtSecurity() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("RetailCore POS API"))
                .andExpect(jsonPath("$.tags[*].name", hasItems(
                        "Authentication",
                        "Categories",
                        "Products",
                        "Inventory",
                        "Sales",
                        "Payments",
                        "Reports"
                )))
                .andExpect(jsonPath("$.components.securitySchemes", hasKey("bearerAuth")))
                .andExpect(jsonPath("$.components.schemas", hasKey("ProductCreateRequest")))
                .andExpect(jsonPath("$.components.schemas", hasKey("CheckoutRequest")))
                .andExpect(jsonPath("$.components.schemas", hasKey("ReceiptResponse")))
                .andExpect(jsonPath("$.components.schemas", hasKey("RefundRequest")));
    }

    @Test
    void swaggerUiPageIsPublic() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", containsString("/swagger-ui/index.html")));
    }
}
