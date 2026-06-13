package com.retailcore.pos.category;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.retailcore.pos.category.dto.CategoryCreateRequest;
import com.retailcore.pos.category.dto.CategoryResponse;
import com.retailcore.pos.category.dto.CategoryUpdateRequest;
import com.retailcore.pos.common.exception.GlobalExceptionHandler;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
        controllers = CategoryController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@Import(GlobalExceptionHandler.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoryService categoryService;

    @Test
    void createRejectsBlankName() throws Exception {
        CategoryCreateRequest request = new CategoryCreateRequest(" ", "Drinks");

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("name"));
    }

    @Test
    void createReturnsCreatedCategoryLocation() throws Exception {
        CategoryResponse response = response(10L, "Beverages", "Drinks", true);
        when(categoryService.create(any(CategoryCreateRequest.class))).thenReturn(response);

        CategoryCreateRequest request = new CategoryCreateRequest("Beverages", "Drinks");

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/categories/10"))
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.name").value("Beverages"));

        verify(categoryService).create(any(CategoryCreateRequest.class));
    }

    @Test
    void findAllReturnsCategories() throws Exception {
        when(categoryService.findAll()).thenReturn(List.of(
                response(1L, "Beverages", "Drinks", true),
                response(2L, "Snacks", null, false)
        ));

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Beverages"))
                .andExpect(jsonPath("$[1].active").value(false));
    }

    @Test
    void findByIdReturnsCategory() throws Exception {
        when(categoryService.findById(1L)).thenReturn(response(1L, "Beverages", "Drinks", true));

        mockMvc.perform(get("/api/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Beverages"));
    }

    @Test
    void updateRequiresActiveFlag() throws Exception {
        CategoryUpdateRequest request = new CategoryUpdateRequest("Beverages", "Drinks", null);

        mockMvc.perform(put("/api/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("active"));
    }

    @Test
    void updateReturnsCategory() throws Exception {
        CategoryResponse response = response(1L, "Snacks", "Chips", false);
        when(categoryService.update(any(Long.class), any(CategoryUpdateRequest.class))).thenReturn(response);

        CategoryUpdateRequest request = new CategoryUpdateRequest("Snacks", "Chips", false);

        mockMvc.perform(put("/api/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Snacks"))
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void deleteReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/categories/1"))
                .andExpect(status().isNoContent());

        verify(categoryService).delete(1L);
    }

    private static CategoryResponse response(Long id, String name, String description, boolean active) {
        return new CategoryResponse(
                id,
                name,
                description,
                active,
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-01T00:00:00Z")
        );
    }
}
