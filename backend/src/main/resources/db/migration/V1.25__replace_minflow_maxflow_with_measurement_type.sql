ALTER TABLE spot_table
    ADD COLUMN measurement_type measurement_type NOT NULL DEFAULT 'FLOW',
    ADD COLUMN min_value REAL,
    ADD COLUMN max_value REAL;

UPDATE spot_table
SET min_value = minflow,
    max_value = maxflow;

ALTER TABLE spot_table
    ALTER COLUMN min_value SET NOT NULL,
    ALTER COLUMN max_value SET NOT NULL,
    DROP COLUMN minflow,
    DROP COLUMN maxflow;
