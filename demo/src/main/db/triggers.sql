DELIMITER //

-- Stock decrement source of truth:
-- OrderService#createOrderWithItems validates/locks stock and inserts order items.
-- This trigger performs the actual decrement once per inserted row.
CREATE TRIGGER trg_reduce_stock
    AFTER INSERT ON orderitems
    FOR EACH ROW
BEGIN

    UPDATE products
    SET stock_quantity = stock_quantity - NEW.quantity
    WHERE id = NEW.product_id;

END//

DELIMITER ;
