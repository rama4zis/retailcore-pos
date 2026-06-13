CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    category_id BIGINT NOT NULL,
    sku VARCHAR(80) NOT NULL,
    barcode VARCHAR(80),
    name VARCHAR(160) NOT NULL,
    description VARCHAR(1000),
    price NUMERIC(12, 2) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories (id),
    CONSTRAINT uk_products_sku UNIQUE (sku),
    CONSTRAINT uk_products_barcode UNIQUE (barcode),
    CONSTRAINT ck_products_price_positive CHECK (price > 0)
);

CREATE INDEX idx_products_category_id ON products (category_id);
CREATE INDEX idx_products_active ON products (active);
