CREATE INDEX idx_customer_email
    ON customers(email);

CREATE INDEX idx_product_name
    ON products(name);

CREATE INDEX idx_order_date
    ON orders(order_date);