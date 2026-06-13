package com.retailcore.pos.inventory;

import com.retailcore.pos.common.exception.ResourceNotFoundException;

public class InventoryStockNotFoundException extends ResourceNotFoundException {

    public InventoryStockNotFoundException(Long productId) {
        super("Inventory stock not found for product id: " + productId);
    }
}
