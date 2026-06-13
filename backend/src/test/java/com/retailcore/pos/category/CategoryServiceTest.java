package com.retailcore.pos.category;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.retailcore.pos.category.dto.CategoryCreateRequest;
import com.retailcore.pos.category.dto.CategoryResponse;
import com.retailcore.pos.category.dto.CategoryUpdateRequest;
import com.retailcore.pos.category.exception.CategoryInUseException;
import com.retailcore.pos.category.exception.CategoryNotFoundException;
import com.retailcore.pos.common.exception.DuplicateResourceException;
import com.retailcore.pos.product.ProductRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void createTrimsNameAndDescription() {
        when(categoryRepository.existsByNameIgnoreCase("Beverages")).thenReturn(false);
        when(categoryRepository.save(any(CategoryEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CategoryCreateRequest request = new CategoryCreateRequest(" Beverages ", " Drinks ");

        CategoryResponse response = categoryService.create(request);

        ArgumentCaptor<CategoryEntity> categoryCaptor = ArgumentCaptor.forClass(CategoryEntity.class);
        verify(categoryRepository).save(categoryCaptor.capture());
        CategoryEntity saved = categoryCaptor.getValue();

        assertThat(saved.getName()).isEqualTo("Beverages");
        assertThat(saved.getDescription()).isEqualTo("Drinks");
        assertThat(saved.isActive()).isTrue();
        assertThat(response.name()).isEqualTo("Beverages");
    }

    @Test
    void createRejectsDuplicateName() {
        when(categoryRepository.existsByNameIgnoreCase("Beverages")).thenReturn(true);

        CategoryCreateRequest request = new CategoryCreateRequest("Beverages", null);

        assertThatThrownBy(() -> categoryService.create(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Category name already exists");

        verify(categoryRepository, never()).save(any());
    }

    @Test
    void findAllReturnsResponses() {
        CategoryEntity beverages = new CategoryEntity("Beverages", "Drinks");
        CategoryEntity snacks = new CategoryEntity("Snacks", null);
        when(categoryRepository.findAll()).thenReturn(List.of(beverages, snacks));

        List<CategoryResponse> responses = categoryService.findAll();

        assertThat(responses)
                .extracting(CategoryResponse::name)
                .containsExactly("Beverages", "Snacks");
    }

    @Test
    void findByIdRejectsMissingCategory() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.findById(99L))
                .isInstanceOf(CategoryNotFoundException.class)
                .hasMessageContaining("Category not found with id: 99");
    }

    @Test
    void updateRejectsDuplicateNameFromAnotherCategory() {
        CategoryEntity category = new CategoryEntity("Beverages", "Drinks");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByNameIgnoreCaseAndIdNot("Snacks", 1L)).thenReturn(true);

        CategoryUpdateRequest request = new CategoryUpdateRequest("Snacks", null, true);

        assertThatThrownBy(() -> categoryService.update(1L, request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Category name already exists");
    }

    @Test
    void updateChangesCategoryFields() {
        CategoryEntity category = new CategoryEntity("Beverages", "Drinks");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByNameIgnoreCaseAndIdNot("Snacks", 1L)).thenReturn(false);

        CategoryResponse response = categoryService.update(1L, new CategoryUpdateRequest(" Snacks ", " Chips ", false));

        assertThat(category.getName()).isEqualTo("Snacks");
        assertThat(category.getDescription()).isEqualTo("Chips");
        assertThat(category.isActive()).isFalse();
        assertThat(response.active()).isFalse();
    }

    @Test
    void deleteRejectsCategoryUsedByProducts() {
        CategoryEntity category = new CategoryEntity("Beverages", "Drinks");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.existsByCategoryId(1L)).thenReturn(true);

        assertThatThrownBy(() -> categoryService.delete(1L))
                .isInstanceOf(CategoryInUseException.class)
                .hasMessageContaining("Category is still used by products");

        verify(categoryRepository, never()).delete(any());
    }

    @Test
    void deleteRemovesUnusedCategory() {
        CategoryEntity category = new CategoryEntity("Beverages", "Drinks");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.existsByCategoryId(1L)).thenReturn(false);

        categoryService.delete(1L);

        verify(categoryRepository).delete(category);
    }
}
