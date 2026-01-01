ALTER TABLE sample_table
    ADD COLUMN country country NOT NULL DEFAULT 'CH';

ALTER TABLE sample_table
    ALTER COLUMN stationid TYPE VARCHAR
    USING stationid::VARCHAR;

ALTER TABLE forecast_table
    ADD COLUMN country country NOT NULL DEFAULT 'CH';

ALTER TABLE forecast_table
    ALTER COLUMN stationid TYPE VARCHAR
    USING stationid::VARCHAR;

ALTER TABLE spot_table
    ADD COLUMN country country NOT NULL DEFAULT 'CH';

ALTER TABLE spot_table
    ALTER COLUMN stationid TYPE VARCHAR
    USING stationid::VARCHAR;

ALTER TABLE historical_years_data_table
    ADD COLUMN country country NOT NULL DEFAULT 'CH';

ALTER TABLE historical_years_data_table
    ALTER COLUMN station_id TYPE VARCHAR
    USING station_id::VARCHAR;

ALTER TABLE last_40_days_samples_table
    ADD COLUMN country country NOT NULL DEFAULT 'CH';

ALTER TABLE last_40_days_samples_table
    ALTER COLUMN station_id TYPE VARCHAR
    USING station_id::VARCHAR;
