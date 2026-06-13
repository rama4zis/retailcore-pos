package com.retailcore.pos.category;

import com.retailcore.pos.category.dto.CategoryCreateRequest;
import com.retailcore.pos.category.dto.CategoryResponse;
import com.retailcore.pos.category.dto.CategoryUpdateRequest;
import com.retailcore.pos.category.exception.CategoryInUseException;
import com.retailcore.pos.category.exception.CategoryNotFoundException;
import com.retailcore.pos.category.exception.DuplicateCategoryNameException;
import com.retailcore.pos.product.ProductRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Transactional
    public CategoryResponse create(CategoryCreateRequest request) {
        String name = normalizeRequired(request.name());
        ensureNameAvailable(name);

        CategoryEntity category = new CategoryEntity(name, request.description());
        CategoryEntity savedCategory = categoryRepository.save(category);

        return CategoryResponse.from(savedCategory);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> findAll() {
        return categoryRepository.findAll()
                .stream()
                .map(CategoryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse findById(Long id) {
        return CategoryResponse.from(getCategory(id));
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryUpdateRequest request) {
        CategoryEntity category = getCategory(id);
        String name = normalizeRequired(request.name());
        ensureNameAvailableForUpdate(name, id);

        category.update(name, request.description());
        category.changeActive(request.active());

        return CategoryResponse.from(category);
    }

    @Transactional
    public void delete(Long id) {
        CategoryEntity category = getCategory(id);
        if (productRepository.existsByCategoryId(id)) {
            throw new CategoryInUseException(id);
        }

        categoryRepository.delete(category);
    }

    private CategoryEntity getCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
    }

    private void ensureNameAvailable(String name) {
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateCategoryNameException(name);
        }
    }

    private void ensureNameAvailableForUpdate(String name, Long id) {
        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new DuplicateCategoryNameException(name);
        }
    }

    private static String normalizeRequired(String value) {
        return value == null ? null : value.trim();
    }
}
