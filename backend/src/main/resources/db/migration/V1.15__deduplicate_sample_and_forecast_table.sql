DELETE FROM forecast_table a
    USING forecast_table b
WHERE a.stationid = b.stationid
  AND a.timestamp = b.timestamp
  AND a.id > b.id;

DELETE FROM sample_table a
    USING sample_table b
WHERE a.stationid = b.stationid
  AND a.timestamp = b.timestamp
  AND a.id > b.id;
