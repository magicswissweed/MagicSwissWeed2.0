CREATE TYPE flowStatus as ENUM (
    'GOOD',
    'TENDENCY_TO_BECOME_GOOD',
    'BAD');

CREATE TABLE spot_current_info_table
(
    id                  UUID        PRIMARY KEY,
    spot_id             UUID        REFERENCES spot_table (id) ON DELETE CASCADE,
    currentFlowStatus   flowStatus  NOT NULL
)
