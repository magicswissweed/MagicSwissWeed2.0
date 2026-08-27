-- Make the station model generic:
--  * "country" becomes a plain ISO 3166-1 alpha-2 code (VARCHAR) instead of the enum CH/FR/DE_BW
--  * the data provider (which API we fetch from) moves into its own column on station_table
--  * the link to the data source (authority page) and the state/region are stored per station

-- 1. provider discriminator + descriptive columns on station_table (backfilled from the old country enum)
CREATE TYPE provider AS ENUM ('HYDRODATEN', 'VIGICRUES', 'HVZ_BW');

ALTER TABLE station_table
    ADD COLUMN provider    provider,
    ADD COLUMN state       VARCHAR,
    ADD COLUMN source_link VARCHAR;

UPDATE station_table
SET provider    = 'HYDRODATEN',
    source_link = 'https://www.hydrodaten.admin.ch/de/seen-und-fluesse/stationen-und-daten/' || stationid
WHERE country = 'CH';

UPDATE station_table
SET provider    = 'VIGICRUES',
    source_link = 'https://www.vigicrues.gouv.fr/station/' || stationid
WHERE country = 'FR';

UPDATE station_table
SET provider    = 'HVZ_BW',
    state       = 'Baden-Württemberg',
    source_link = 'https://www.hvz.baden-wuerttemberg.de/pegel.html?id=' || stationid
WHERE country = 'DE_BW';

ALTER TABLE station_table
    ALTER COLUMN provider SET NOT NULL;

-- 2. country enum -> varchar on all tables referencing a station
ALTER TABLE station_table
    ALTER COLUMN country DROP DEFAULT,
    ALTER COLUMN country TYPE VARCHAR USING country::text;

ALTER TABLE sample_table
    ALTER COLUMN country DROP DEFAULT,
    ALTER COLUMN country TYPE VARCHAR USING country::text;

ALTER TABLE forecast_table
    ALTER COLUMN country DROP DEFAULT,
    ALTER COLUMN country TYPE VARCHAR USING country::text;

ALTER TABLE spot_table
    ALTER COLUMN country DROP DEFAULT,
    ALTER COLUMN country TYPE VARCHAR USING country::text;

ALTER TABLE historical_years_data_table
    ALTER COLUMN country DROP DEFAULT,
    ALTER COLUMN country TYPE VARCHAR USING country::text;

DROP TYPE country;

-- 3. DE_BW was never a country: Baden-Württemberg stations live in Germany
UPDATE station_table SET country = 'DE' WHERE country = 'DE_BW';
UPDATE sample_table SET country = 'DE' WHERE country = 'DE_BW';
UPDATE forecast_table SET country = 'DE' WHERE country = 'DE_BW';
UPDATE spot_table SET country = 'DE' WHERE country = 'DE_BW';
UPDATE historical_years_data_table SET country = 'DE' WHERE country = 'DE_BW';
