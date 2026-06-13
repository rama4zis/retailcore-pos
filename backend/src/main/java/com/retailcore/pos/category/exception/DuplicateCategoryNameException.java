package com.retailcore.pos.category.exception;

import com.retailcore.pos.common.exception.DuplicateResourceException;

public class DuplicateCategoryNameException extends DuplicateResourceException {

    public DuplicateCategoryNameException(String name) {
        super("Category name already exists: " + name);
    }
}
