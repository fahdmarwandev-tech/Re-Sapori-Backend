-- liquibase formatted sql

-- changeset resapori:35-alter-payment-method-enum runInTransaction:false
-- comment: Add INSTAPAY, VODAFONE_CASH, and CASH to payment_method enum.
--          Must run outside a transaction (runInTransaction:false) because PostgreSQL
--          does not allow ALTER TYPE ADD VALUE inside a transaction block.

ALTER TYPE payment_method ADD VALUE IF NOT EXISTS 'INSTAPAY';
ALTER TYPE payment_method ADD VALUE IF NOT EXISTS 'VODAFONE_CASH';
ALTER TYPE payment_method ADD VALUE IF NOT EXISTS 'CASH';
