# DBS Project – Order Management System / Webshop Backend

## 1. Project Overview

This project is a backend service for a small order management system/webshop, developed as part of a Database Systems course.

The application provides a REST API for managing customers, products, product categories, suppliers, and orders. It uses a relational MariaDB database and demonstrates several database techniques covered during the course, including:

- CRUD operations through a REST API
- Relational data modeling with JPA entities
- Database views
- Database triggers
- Database indexes
- Transaction management with Spring
- Separation into controller, service, repository, and entity layers

> **Project status:** This is an academic prototype. The basic CRUD API and database features are implemented. Production-level features such as authentication, authorization, advanced validation, centralized error handling, and a complete order-with-items workflow can be extended in future versions.

## 2. Technology Stack

- Java 17
- Spring Boot 4.1.1
- Spring WebMVC
- Spring Data JPA / Hibernate
- MariaDB
- Maven Wrapper
- JUnit and Spring Boot Test

## 3. Project Structure

```text
DBS_Project/
└── demo/
    ├── pom.xml
    ├── mvnw
    ├── mvnw.cmd
    └── src/
        ├── main/
        │   ├── java/com/example/demo/
        │   │   ├── DemoApplication.java
        │   │   ├── controller/       # REST controllers
        │   │   ├── entity/           # JPA entities
        │   │   ├── repository/       # Spring Data repositories
        │   │   └── service/          # Service and business layer
        │   ├── db/
        │   │   ├── indexes.sql       # Database indexes
        │   │   ├── triggers.sql      # Database triggers
        │   │   └── views.sql         # Database views
        │   └── resources/
        │       └── application.properties
        └── test/
            └── java/com/example/demo/
                └── DemoApplicationTests.java
```

## 4. Data Model

The main entities are:

| Entity | Database table | Description |
|---|---|---|
| `Customer` | `customers` | Customer information |
| `CustomerAddress` | Customer address table | Addresses belonging to customers |
| `Product` | `products` | Products available for sale |
| `ProductCategory` | Product category table | Product categories |
| `Supplier` | `suppliers` | Product suppliers |
| `SupplierAddress` | Supplier address table | Supplier addresses |
| `Order` | `orders` | Customer orders |
| `OrderItem` | `orderitems` | Products included in an order |

A product can belong to a category and a supplier. An order is associated with a customer and can contain multiple order items. `OrderItem` uses a composite key through the `OrderItemId` class.

## 5. Requirements

Install the following software before running the project:

- JDK 17 or newer
- MariaDB Server
- Git

Create the database:

```sql
CREATE DATABASE database_solution;
```

The default application configuration uses:

```text
Host: localhost
Port: 3306
Database: database_solution
Username: root
Application port: 8084
```

## 6. Configuration

The database configuration is located in:

```text
src/main/resources/application.properties
```

The recommended configuration is:

```properties
spring.datasource.url=${DB_URL:jdbc:mariadb://localhost:3306/database_solution}
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD}

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

server.port=${SERVER_PORT:8084}
```

For security reasons, database passwords should be provided through environment variables and should not be committed to the repository.

## 7. Running the Application

From the `demo` directory, run:

```bash
./mvnw clean install
./mvnw spring-boot:run
```

On Windows:

```bat
mvnw.cmd clean install
mvnw.cmd spring-boot:run
```

The application will be available at:

```text
http://localhost:8084
```

Run the tests with:

```bash
./mvnw test
```

## 8. REST API Documentation

The base URL is:

```text
http://localhost:8084
```

The API uses JSON for request and response bodies.

### 8.1 Customer API

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/customers` | Get all customers |
| `GET` | `/customers/{id}` | Get a customer by ID |
| `POST` | `/customers` | Create a customer |
| `PUT` | `/customers/{id}` | Update a customer |
| `DELETE` | `/customers/{id}` | Delete a customer |

Example request:

```http
POST /customers
Content-Type: application/json
```

```json
{
  "firstName": "Anna",
  "lastName": "Smith",
  "email": "anna.smith@example.com",
  "phone": "+358401234567"
}
```

Example response:

```json
{
  "customerId": 1,
  "firstName": "Anna",
  "lastName": "Smith",
  "email": "anna.smith@example.com",
  "phone": "+358401234567"
}
```

### 8.2 Product API

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/products` | Get all products |
| `GET` | `/products/{id}` | Get a product by ID |
| `POST` | `/products` | Create a product |
| `PUT` | `/products/{id}` | Update a product |
| `DELETE` | `/products/{id}` | Delete a product |

Example request:

```http
POST /products
Content-Type: application/json
```

```json
{
  "productName": "Mechanical Keyboard",
  "description": "Mechanical keyboard with RGB lighting",
  "price": 79.90,
  "stockQuantity": 25
}
```

Example response:

```json
{
  "productId": 1,
  "productName": "Mechanical Keyboard",
  "description": "Mechanical keyboard with RGB lighting",
  "price": 79.90,
  "stockQuantity": 25
}
```

### 8.3 Order API

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/orders` | Get all orders |
| `GET` | `/orders/{id}` | Get an order by ID |
| `POST` | `/orders` | Create only an order header (simple CRUD) |
| `POST` | `/orders/checkout` | Create an order with items in one transaction (recommended webshop flow) |
| `PUT` | `/orders/{id}` | Update an order |
| `DELETE` | `/orders/{id}` | Delete an order |

Example request:

```http
POST /orders
Content-Type: application/json
```

```json
{
  "customerId": 1,
  "orderDate": "2026-10-07T10:30:00",
  "deliveryDate": "2026-10-10T10:30:00",
  "shippingAddressId": 2,
  "status": "PENDING"
}
```

Example response:

```json
{
  "orderId": 1,
  "customerId": 1,
  "orderDate": "2026-10-07T10:30:00",
  "deliveryDate": "2026-10-10T10:30:00",
  "shippingAddressId": 2,
  "status": "PENDING"
}
```

> `POST /orders` remains for simple CRUD on the order header. For webshop checkout, use `POST /orders/checkout` so order + items are created atomically in one transaction.

### 8.3.1 Checkout API (Transactional Order + Items)

```http
POST /orders/checkout
Content-Type: application/json
```

Request example:

```json
{
  "customerId": 1,
  "deliveryDate": "2026-10-10T10:30:00",
  "shippingAddressId": 2,
  "items": [
    { "productId": 10, "quantity": 2 },
    { "productId": 15, "quantity": 1 }
  ]
}
```

Success response (`201 Created`) example:

```json
{
  "orderId": 101,
  "customerId": 1,
  "orderDate": "2026-10-07T12:00:00",
  "deliveryDate": "2026-10-10T10:30:00",
  "shippingAddressId": 2,
  "status": "PENDING",
  "items": [
    { "productId": 10, "quantity": 2, "unitPrice": 79.90 },
    { "productId": 15, "quantity": 1, "unitPrice": 12.50 }
  ]
}
```

Error response shape (used for `400`, `404`, `409`) example:

```json
{
  "timestamp": "2026-10-07T12:00:00",
  "status": 409,
  "error": "Conflict",
  "message": "Insufficient stock for product 10",
  "path": "/orders/checkout"
}
```

Status mapping:
- `400 Bad Request`: invalid request body (missing fields, empty items, non-positive quantity).
- `404 Not Found`: missing customer or product.
- `409 Conflict`: duplicate product in one order or insufficient stock.

### 8.4 Order Item API

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/orders/{orderId}/items` | Get all items for an order |
| `GET` | `/orders/{orderId}/items/{productId}` | Get one item by composite key |
| `POST` | `/orders/{orderId}/items` | Create an item for an order |
| `PUT` | `/orders/{orderId}/items/{productId}` | Update an existing order item |
| `DELETE` | `/orders/{orderId}/items/{productId}` | Delete an order item |

Example request:

```http
POST /orders/1/items
Content-Type: application/json
```

```json
{
  "id": {
    "orderId": 999,
    "productId": 10
  },
  "quantity": 2,
  "unitPrice": 79.90
}
```

Example response:

```json
{
  "id": {
    "orderId": 1,
    "productId": 10
  },
  "quantity": 2,
  "unitPrice": 79.90
}
```

> `orderId` is always taken from the path. If request body `id.orderId` conflicts, the path value is applied.
>
> Stock reduction behavior for new order items is handled by a database trigger (`src/main/db/triggers.sql`). This SQL trigger is database-side and may need to be installed manually in MariaDB before testing this behavior.

### 8.5 Category API

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/categories` | Get all categories |
| `GET` | `/categories/{id}` | Get a category by ID |
| `POST` | `/categories` | Create a category |
| `DELETE` | `/categories/{id}` | Delete a category |

The current `CategoryController` does not yet provide a `PUT` endpoint.

### 8.6 Supplier API

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/suppliers` | Get all suppliers |
| `GET` | `/suppliers/{id}` | Get a supplier by ID |
| `POST` | `/suppliers` | Create a supplier |
| `DELETE` | `/suppliers/{id}` | Delete a supplier |

The current `SupplierController` does not yet provide a `PUT` endpoint.

## 9. Database Features

### 9.1 Database View

The file `src/main/db/views.sql` defines the following view:

```sql
CREATE VIEW order_summary_view AS
SELECT
    o.id,
    c.first_name,
    c.last_name,
    o.order_date,
    o.status
FROM orders o
JOIN customers c ON c.id = o.customer_id;
```

The view combines order information with customer information. It provides a reusable summary query and avoids repeating the same join in different database queries.

### 9.2 Database Trigger

The file `src/main/db/triggers.sql` defines the trigger `trg_reduce_stock`:

```sql
CREATE TRIGGER trg_reduce_stock
AFTER INSERT ON orderitems
FOR EACH ROW
BEGIN
    UPDATE products
    SET stock_quantity = stock_quantity - NEW.quantity
    WHERE id = NEW.product_id;
END;
```

The trigger is the single stock-decrement source of truth in this project: stock is reduced after each `orderitems` insert.

> The trigger is implemented in SQL and runs in MariaDB. If your local database does not yet contain it, execute `src/main/db/triggers.sql` manually after schema creation.

`OrderService#createOrderWithItems` does **not** decrement stock in Java to avoid double-decrement. Instead, it locks products (`PESSIMISTIC_WRITE`) and validates stock before inserting order items; then the trigger performs the decrement exactly once.

### 9.3 Database Indexes

The file `src/main/db/indexes.sql` defines the following indexes:

```sql
CREATE INDEX idx_customer_email ON customers(email);
CREATE INDEX idx_product_name ON products(name);
CREATE INDEX idx_order_date ON orders(order_date);
```

Purpose of the indexes:

- `idx_customer_email`: improves customer lookup by email.
- `idx_product_name`: improves product lookup by name.
- `idx_order_date`: improves filtering and sorting orders by date.

The indexes can be inspected with MariaDB using `EXPLAIN`:

```sql
EXPLAIN SELECT *
FROM customers
WHERE email = 'anna.smith@example.com';

EXPLAIN SELECT *
FROM products
WHERE name = 'Mechanical Keyboard';

EXPLAIN SELECT *
FROM orders
ORDER BY order_date DESC;
```

### 9.4 Transactions

`OrderService#createOrderWithItems` now uses a single `@Transactional` workflow for checkout:

1. Validate request shape (required fields, non-empty items, positive quantities).
2. Validate customer existence.
3. Reject duplicate product IDs in the same order.
4. Lock each product row (`PESSIMISTIC_WRITE`), validate product existence, and validate stock.
5. Create and persist the order (default `orderDate=now`, `status=PENDING` when omitted).
6. Create and persist order items with unit price snapshots from current product prices.
7. Let `trg_reduce_stock` decrement stock once for each inserted order item.

If any validation or persistence step fails, Spring rolls the whole transaction back, so partial order headers/items are not committed.

## 10. Testing

The project includes unit tests for application context loading, `OrderItem` routing/service logic, and checkout workflow validation, including:

- successful multi-item checkout
- missing customer/product
- non-positive quantity
- duplicate product ID
- insufficient stock with no persistence side-effects
- unit-price snapshot behavior

Run all tests with:

```bash
./mvnw test
```

Recommended additional tests include:

- Customer CRUD tests
- Product CRUD tests
- Order CRUD tests
- Stock reduction trigger tests
- Insufficient stock tests
- Transaction rollback tests
- Database view tests
- Validation and error response tests

## 11. Security and Current Limitations

The current version does not yet implement:

- User authentication
- Authorization and role-based access control
- Customer and administrator roles
- Password hashing
- Full Bean Validation (`@Valid`) coverage on all endpoints
- OpenAPI/Swagger documentation
- Temporal or audit history for order status changes

This project is an academic database and REST API prototype. For production use, the application should use a dedicated database user instead of `root`, externalized secrets, authentication, authorization, validation, and structured error handling.

## 12. Future Improvements

Possible future improvements include:

- Add integration tests against MariaDB trigger behavior (`trg_reduce_stock`) in CI.
- Restore stock when an order is cancelled or items are removed.
- Add order status history and audit tables.
- Add authentication and role-based authorization.
- Add Swagger/OpenAPI documentation.
- Add integration tests for views, triggers, indexes, and transactions.

## 13. Author

Project repository: [Hoang-Vuu/DBS_Project](https://github.com/Hoang-Vuu/DBS_Project)
