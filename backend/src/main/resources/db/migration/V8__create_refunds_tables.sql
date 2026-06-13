ALTER TABLE sales DROP CONSTRAINT ck_sales_status;
ALTER TABLE sales ADD CONSTRAINT ck_sales_status CHECK (status IN ('COMPLETED', 'PARTIALLY_REFUNDED', 'REFUNDED'));

ALTER TABLE stock_movements ADD CONSTRAINT ck_stock_movements_type CHECK (movement_type IN ('ADJUSTMENT', 'SALE', 'REFUND'));

CREATE TABLE refunds (
    id BIGSERIAL PRIMARY KEY,
    sale_id BIGINT NOT NULL,
    total_amount NUMERIC(12, 2) NOT NULL,
    reason VARCHAR(500),
    refunded_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_refunds_sale FOREIGN KEY (sale_id) REFERENCES sales (id),
    CONSTRAINT ck_refunds_total_amount_non_negative CHECK (total_amount >= 0)
);

CREATE TABLE refund_items (
    id BIGSERIAL PRIMARY KEY,
    refund_id BIGINT NOT NULL,
    sale_item_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    sku VARCHAR(80) NOT NULL,
    product_name VARCHAR(160) NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price NUMERIC(12, 2) NOT NULL,
    line_total NUMERIC(12, 2) NOT NULL,
    CONSTRAINT fk_refund_items_refund FOREIGN KEY (refund_id) REFERENCES refunds (id),
    CONSTRAINT fk_refund_items_sale_item FOREIGN KEY (sale_item_id) REFERENCES sale_items (id),
    CONSTRAINT fk_refund_items_product FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT ck_refund_items_quantity_positive CHECK (quantity > 0),
    CONSTRAINT ck_refund_items_unit_price_non_negative CHECK (unit_price >= 0),
    CONSTRAINT ck_refund_items_line_total_non_negative CHECK (line_total >= 0)
);

CREATE INDEX idx_refunds_sale_id ON refunds (sale_id);
CREATE INDEX idx_refunds_refunded_at ON refunds (refunded_at);
CREATE INDEX idx_refund_items_refund_id ON refund_items (refund_id);
CREATE INDEX idx_refund_items_sale_item_id ON refund_items (sale_item_id);
CREATE INDEX idx_refund_items_product_id ON refund_items (product_id);
