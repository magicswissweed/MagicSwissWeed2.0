-- Seed sample_table with synthetic FLOW and TEMPERATURE data for the last 10
-- days, for every station currently in station_table. Values are not real —
-- just a sine wave with jitter, scaled per-station — but enough to render the
-- "last few days" graph (and exercise the measurement-type switcher) in the UI
-- without waiting for live fetches to accumulate.
--
-- Run against the local dev DB:
--   docker exec -i msw-postgres psql -U backend -d msw < localdev/seed-samples.sql
--
-- Idempotent: relies on the unique_sample constraint (timestamp, stationid,
-- measurement_type) to skip rows that already exist.


--     uncomment this to delete samples of last 10 days
DELETE
FROM sample_table
WHERE timestamp >= now() - INTERVAL '40 days';

INSERT INTO sample_table (id, stationid, country, timestamp, value, measurement_type)
SELECT gen_random_uuid(),
       s.stationid,
       s.country,
       ts,
       GREATEST(
               0,
               20 + (abs(hashtext(s.stationid)) % 50)
                   + 15 * sin(extract(epoch FROM ts) / 7200.0)
                   + 5 * random()
       )::real,
       'FLOW'::measurement_type
FROM station_table s
         CROSS JOIN generate_series(
        now() - INTERVAL '40 days',
        now(),
        INTERVAL '30 minutes'
                    ) AS ts
ON CONFLICT (timestamp, stationid, measurement_type) DO NOTHING;

-- Synthetic water TEMPERATURE (°C): a daily cycle around a per-station baseline,
-- clamped to a plausible 0–30 °C range.
INSERT INTO sample_table (id, stationid, country, timestamp, value, measurement_type)
SELECT gen_random_uuid(),
       s.stationid,
       s.country,
       ts,
       LEAST(30, GREATEST(
               0,
               10 + (abs(hashtext(s.stationid)) % 8)
                   + 4 * sin(extract(epoch FROM ts) / 43200.0)
                   + random()
                 ))::real,
       'TEMPERATURE'::measurement_type
FROM station_table s
         CROSS JOIN generate_series(
        now() - INTERVAL '40 days',
        now(),
        INTERVAL '30 minutes'
                    ) AS ts
ON CONFLICT (timestamp, stationid, measurement_type) DO NOTHING;

SELECT country,
       measurement_type,
       count(*) AS samples_in_last_40_days
FROM sample_table
WHERE timestamp >= now() - INTERVAL '40 days'
GROUP BY country, measurement_type
ORDER BY country, measurement_type;
