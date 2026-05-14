CREATE INDEX sample_table_lookup_idx
    ON sample_table (country, stationid, measurement_type, timestamp DESC);
