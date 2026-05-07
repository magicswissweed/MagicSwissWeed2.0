CREATE TYPE measurement_type AS ENUM ('FLOW', 'HEIGHT', 'TEMPERATURE');

ALTER TABLE sample_table
    ADD COLUMN measurement_type measurement_type NOT NULL DEFAULT 'FLOW';

ALTER TABLE forecast_table
    ADD COLUMN measurement_type measurement_type NOT NULL DEFAULT 'FLOW';

ALTER TABLE historical_years_data_table
    ADD COLUMN measurement_type measurement_type NOT NULL DEFAULT 'FLOW';

ALTER TABLE last_few_days_samples_table
    ADD COLUMN measurement_type measurement_type NOT NULL DEFAULT 'FLOW';

ALTER TABLE sample_table DROP CONSTRAINT unique_sample;
ALTER TABLE sample_table
    ADD CONSTRAINT unique_sample UNIQUE (timestamp, stationid, measurement_type);

ALTER TABLE forecast_table DROP CONSTRAINT unique_forecast;
ALTER TABLE forecast_table
    ADD CONSTRAINT unique_forecast UNIQUE (timestamp, stationid, measurement_type);
