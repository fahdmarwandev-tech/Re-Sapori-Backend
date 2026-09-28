-- liquibase formatted sql

-- changeset resapori:36-alter-branches-add-location
-- comment: Add lat and lng columns to branches table

ALTER TABLE branches
    ADD COLUMN lat NUMERIC(10, 7),
    ADD COLUMN lng NUMERIC(10, 7);
