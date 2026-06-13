package com.retailcore.pos.category.exception;

public class CategoryInUseException extends RuntimeException {

    public CategoryInUseException(Long id) {
        super("Category is still used by products and cannot be deleted: " + id);
    }
}
