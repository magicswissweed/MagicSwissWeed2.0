CREATE TYPE country as ENUM (
    'CH',
    'FR');

ALTER TABLE station_table
    ADD COLUMN country country NOT NULL DEFAULT 'CH';
