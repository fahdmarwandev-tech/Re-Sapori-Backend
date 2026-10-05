-- Liquibase formatted SQL
-- changeset resapori:40-alter-branches-add-delivery-zones
ALTER TABLE branches
  ADD COLUMN delivery_zones TEXT;
