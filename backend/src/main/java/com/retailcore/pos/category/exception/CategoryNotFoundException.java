package com.retailcore.pos.category.exception;

public class CategoryNotFoundException extends com.retailcore.pos.common.exception.ResourceNotFoundException {

    public CategoryNotFoundException(Long id) {
        super("Category not found with id: " + id);
    }
}
