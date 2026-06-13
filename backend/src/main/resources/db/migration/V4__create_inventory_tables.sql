CREATE TABLE inventory_stocks (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 0,
    low_stock_threshold INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_inventory_stocks_product FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT uk_inventory_stocks_product UNIQUE (product_id),
    CONSTRAINT ck_inventory_stocks_quantity_non_negative CHECK (quantity >= 0),
    CONSTRAINT ck_inventory_stocks_low_stock_threshold_non_negative CHECK (low_stock_threshold >= 0)
);

CREATE TABLE stock_movements (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    movement_type VARCHAR(40) NOT NULL,
    quantity_change INTEGER NOT NULL,
    stock_after INTEGER NOT NULL,
    reason VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_stock_movements_product FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT ck_stock_movements_quantity_change_non_zero CHECK (quantity_change <> 0),
    CONSTRAINT ck_stock_movements_stock_after_non_negative CHECK (stock_after >= 0)
);

CREATE INDEX idx_inventory_stocks_product_id ON inventory_stocks (product_id);
CREATE INDEX idx_inventory_stocks_low_stock ON inventory_stocks (quantity, low_stock_threshold);
CREATE INDEX idx_stock_movements_product_id ON stock_movements (product_id);
CREATE INDEX idx_stock_movements_created_at ON stock_movements (created_at);
