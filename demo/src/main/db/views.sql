CREATE VIEW order_summary_view AS
SELECT
    o.id,
    c.first_name,
    c.last_name,
    o.order_date,
    o.status
FROM orders o
         JOIN customers c
              ON c.id = o.customer_id;