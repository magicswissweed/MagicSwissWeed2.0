ALTER TABLE forecast_table
    ADD CONSTRAINT unique_forecast UNIQUE (timestamp, stationid);

ALTER TABLE sample_table
    ADD CONSTRAINT unique_sample UNIQUE (timestamp, stationid);
