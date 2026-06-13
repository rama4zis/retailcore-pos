package com.retailcore.pos.sale;

import com.retailcore.pos.common.exception.ResourceNotFoundException;

public class SaleNotFoundException extends ResourceNotFoundException {

    public SaleNotFoundException(Long id) {
        super("Sale not found with id: " + id);
    }
}
