CREATE TABLE sales (
    id BIGSERIAL PRIMARY KEY,
    sale_number VARCHAR(80) NOT NULL,
    cashier_id BIGINT NOT NULL,
    status VARCHAR(40) NOT NULL,
    total_amount NUMERIC(12, 2) NOT NULL,
    completed_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_sales_sale_number UNIQUE (sale_number),
    CONSTRAINT fk_sales_cashier FOREIGN KEY (cashier_id) REFERENCES users (id),
    CONSTRAINT ck_sales_status CHECK (status IN ('COMPLETED')),
    CONSTRAINT ck_sales_total_amount_non_negative CHECK (total_amount >= 0)
);

CREATE TABLE sale_items (
    id BIGSERIAL PRIMARY KEY,
    sale_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    sku VARCHAR(80) NOT NULL,
    product_name VARCHAR(160) NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price NUMERIC(12, 2) NOT NULL,
    line_total NUMERIC(12, 2) NOT NULL,
    CONSTRAINT fk_sale_items_sale FOREIGN KEY (sale_id) REFERENCES sales (id),
    CONSTRAINT fk_sale_items_product FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT ck_sale_items_quantity_positive CHECK (quantity > 0),
    CONSTRAINT ck_sale_items_unit_price_non_negative CHECK (unit_price >= 0),
    CONSTRAINT ck_sale_items_line_total_non_negative CHECK (line_total >= 0)
);

CREATE INDEX idx_sales_cashier_id ON sales (cashier_id);
CREATE INDEX idx_sales_completed_at ON sales (completed_at);
CREATE INDEX idx_sale_items_sale_id ON sale_items (sale_id);
CREATE INDEX idx_sale_items_product_id ON sale_items (product_id);
