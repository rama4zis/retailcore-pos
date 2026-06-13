CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    sale_id BIGINT NOT NULL,
    method VARCHAR(40) NOT NULL,
    amount NUMERIC(12, 2) NOT NULL,
    cash_tendered NUMERIC(12, 2),
    change_amount NUMERIC(12, 2) NOT NULL,
    paid_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_payments_sale_id UNIQUE (sale_id),
    CONSTRAINT fk_payments_sale FOREIGN KEY (sale_id) REFERENCES sales (id),
    CONSTRAINT ck_payments_method CHECK (method IN ('CASH', 'CARD')),
    CONSTRAINT ck_payments_amount_non_negative CHECK (amount >= 0),
    CONSTRAINT ck_payments_cash_tendered_non_negative CHECK (cash_tendered IS NULL OR cash_tendered >= 0),
    CONSTRAINT ck_payments_change_amount_non_negative CHECK (change_amount >= 0),
    CONSTRAINT ck_payments_cash_requires_tendered CHECK (method <> 'CASH' OR cash_tendered IS NOT NULL),
    CONSTRAINT ck_payments_card_has_no_cash_tendered CHECK (method <> 'CARD' OR cash_tendered IS NULL)
);

CREATE INDEX idx_payments_sale_id ON payments (sale_id);
CREATE INDEX idx_payments_method ON payments (method);
CREATE INDEX idx_payments_paid_at ON payments (paid_at);
