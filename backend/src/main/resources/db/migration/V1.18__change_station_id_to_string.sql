ALTER TABLE station_table
    ALTER COLUMN stationid TYPE VARCHAR
    USING stationid::VARCHAR;
