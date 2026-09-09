-- liquibase formatted sql

-- changeset resapori:27-rename-order-status-enum runInTransaction:false
-- comment: Rename lowercase order_status values to uppercase to match Java enum and other DB enums.
--          Must run outside a transaction (runInTransaction:false) because PostgreSQL
--          does not allow ALTER TYPE RENAME VALUE inside a transaction block.

ALTER TYPE order_status RENAME VALUE 'pending' TO 'PENDING';
ALTER TYPE order_status RENAME VALUE 'paid' TO 'PAID';
ALTER TYPE order_status RENAME VALUE 'preparing' TO 'PREPARING';
ALTER TYPE order_status RENAME VALUE 'ready' TO 'READY';
ALTER TYPE order_status RENAME VALUE 'delivered' TO 'DELIVERED';
ALTER TYPE order_status RENAME VALUE 'cancelled' TO 'CANCELLED';
