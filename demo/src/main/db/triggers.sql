DELIMITER //

CREATE TRIGGER trg_reduce_stock
    AFTER INSERT ON orderitems
    FOR EACH ROW
BEGIN

    UPDATE products
    SET stock_quantity = stock_quantity - NEW.quantity
    WHERE id = NEW.product_id;

END//

DELIMITER ;