# Rivermap integration — Step 1: make station model generic (refactor only)

## Context

We want to import stations from the external [Rivermap API](https://api.rivermap.org/) (`GET /v2/stations`) and later their readings (`/v2/stations/readings`). Rivermap spans many countries and, per country, multiple states with different data sources. Our current model can't express that: `CountryEnum` (`CH | FR | DE_BW`) is simultaneously the country, the data provider, and the key by which the fetchers and the frontend (source link, flag) discriminate stations. `DE_BW` already shows the enum is really a *provider*, not a country.

Decision (confirmed with the user): **first do a pure refactor** that makes the model generic and migrates existing data, with tests guarding the station data; **then** add Rivermap (stations, then readings) in follow-up steps. Rivermap: online stations only; source link served by the backend for all stations; no source *name* for now (link only).

Branch: `feature/rivermap`. Persistence is jOOQ + Flyway (no JPA); OpenAPI spec `backend/src/main/resources/api/mswApi.yaml` generates both backend (`openApiGenerateSpring`) and frontend (`openApiGenerateReact`) clients — always regenerate both and check in the generated code (`backend/README.md`).

### Naming: `provider` vs `source`
- **`provider`** = *which API we call to get a station's data* (hydrodaten/existenz, Vigicrues, HVZ‑BW, Rivermap). Internal fetcher discriminator; drives the scheduled jobs and `canFetchData`. Rivermap **is** a provider: for its stations we never call the authority ourselves. **Backend‑only**: persisted on `station_table` because the station cache is rebuilt from the DB on restart and, with `country` a plain ISO code, nothing else tells the Swiss job to skip a Rivermap `CH` station (e.g. Etzgen/ag.ch). Never sent to the frontend — the frontend needs only `id.country`, `sourceLink` (and optionally `state`).
- **`source`** = *the actual authority behind the data* (BAFU, Kanton Aargau, CH Cantábrico, HND Bayern…). Represented today only by the per‑station **`source_link`** (full URL the user is routed to). Rivermap is a wrapper around many sources — it is never a `source`. A `source_name` column can be added later if the UI needs attribution text (Rivermap exposes it via `dataSourceId` → `sources[]`).

---

## Target model (after step 1)

| Concept | Before | After |
|---|---|---|
| `ApiStationId.country` | `CountryEnum` (CH/FR/DE_BW) | **plain `string`, ISO‑3166‑1 alpha‑2** (`CH`, `FR`, `DE`, …). Business key stays `(country, externalId)`. |
| DB `country` column (5 tables) | PG enum `country` | `VARCHAR`; `DE_BW` rows → `DE` |
| Data provider | implicit in `CountryEnum` | new PG enum `provider` + `station_table.provider NOT NULL` = `HYDRODATEN \| VIGICRUES \| HVZ_BW` (step 2 adds `RIVERMAP`). Used **only** on `station_table`; fetchers resolve it via the station. Not exposed in the API. |
| State/region | – | `station_table.state VARCHAR NULL` (`Baden-Württemberg` for HVZ_BW, null otherwise) |
| Link to the source (authority) page | built in frontend `switch` | `station_table.source_link VARCHAR NULL` = **full URL incl. externalId**, exposed as optional `ApiStation.sourceLink`; frontend just renders it |
| Flag | frontend `switch` on enum | computed generically from ISO code (`🇨🇭` from `CH`) |

Key-collision note: `(country, externalId)` remains the identity. `unique_sample` already omits `country` (`V1.24`), so the system de facto relies on externalIds being distinct across providers — true today (numeric CH, alnum FR, zero‑padded BW, UUIDs for Rivermap). Acceptable; a future `(provider, externalId)` key is out of scope.

---

## Step 1 — Implementation

### 1. Migration `backend/src/main/resources/db/migration/V1.30__generic_country_and_station_provider.sql`
(latest is `V1.29`; `V1.21` gap is fine)

Order matters — backfill from the old enum values *before* converting them:

```sql
-- new provider discriminator + descriptive columns on station_table
CREATE TYPE provider AS ENUM ('HYDRODATEN', 'VIGICRUES', 'HVZ_BW');
ALTER TABLE station_table
    ADD COLUMN provider    provider,
    ADD COLUMN state       VARCHAR,
    ADD COLUMN source_link VARCHAR;
UPDATE station_table SET provider = 'HYDRODATEN',
    source_link = 'https://www.hydrodaten.admin.ch/de/seen-und-fluesse/stationen-und-daten/' || stationid WHERE country = 'CH';
UPDATE station_table SET provider = 'VIGICRUES',
    source_link = 'https://www.vigicrues.gouv.fr/station/' || stationid WHERE country = 'FR';
UPDATE station_table SET provider = 'HVZ_BW', state = 'Baden-Württemberg',
    source_link = 'https://www.hvz.baden-wuerttemberg.de/pegel.html?id=' || stationid WHERE country = 'DE_BW';
ALTER TABLE station_table ALTER COLUMN provider SET NOT NULL;

-- country enum -> varchar on all 5 tables (station_table, sample_table, forecast_table, spot_table, historical_years_data_table)
ALTER TABLE <t> ALTER COLUMN country DROP DEFAULT,
                ALTER COLUMN country TYPE VARCHAR USING country::text;   -- x5 (historical_years_data_table too)
DROP TYPE country;
UPDATE <t> SET country = 'DE' WHERE country = 'DE_BW';                    -- x5
```
Notes: `sample_table_lookup_idx (country, …)` is rebuilt automatically by the type change; `sample_table` is the big one (300‑day retention) → the migration rewrites it once; plan a short deploy window. `CREATE TYPE` + use in same transaction is fine (only `ADD VALUE` has that restriction).

### 2. Regenerate jOOQ
`./gradlew :backend:generateJooq` (needs the root `docker-compose.yml` DB running; task depends on `flywayMigrate` against `gradlecodegen`). Generated `com.aa.msw.gen.jooq.enums.Country` disappears, `…enums.Provider` appears, `StationTable` gains `PROVIDER`, `STATE`, `SOURCE_LINK`. Compile errors then enumerate every site to fix.

### 3. OpenAPI `mswApi.yaml` + regenerate both clients
- Delete `CountryEnum` schema; `ApiStationId.country` → `type: string` (description: ISO 3166‑1 alpha‑2).
- `ApiStation`: add optional `sourceLink: string` and `state: string` (not in `required`).
- Fix stale summary "from BAFU" on `/api/v1/stations` while there.
- Run `openApiGenerateSpring` **and** `openApiGenerateReact`, commit generated code (`backend/src/generated/api`, `frontend/src/gen/msw-api-ts`).

### 4. Backend domain & repositories
- `model/Station.java`: add `Provider provider`, `String state`, `String sourceLink` (use the jOOQ‑generated `com.aa.msw.gen.jooq.enums.Provider` directly; test mocks already import jOOQ enums).
- `database/helpers/EnumConverterHelper.java`: `apiStationId(String country, String stationid)`; delete `country(CountryEnum)`.
- `database/repository/StationRepository.java`: map the 3 new columns in `mapRecord`/`mapDomain`/`mapEntity`; `deleteByStationId` compares `TABLE.COUNTRY.eq(stationId.getCountry())`.
- `SampleRepository`, `ForecastRepository`, `SpotRepository`, `HistoricalYearsDataRepository`: drop `country(...)` wrapping — `TABLE.COUNTRY` is now a `String` field (`SampleRepository.java:44,54,66,76,88,133,137`; `ForecastRepository.java:62,95,112,156`; `SpotRepository.java:44,57,73,109,118-120`; `HistoricalYearsDataRepository.java:84,99,115`).
- `SpotDao.getReferencedStationIds(CountryEnum)` → `getReferencedStationIds()` (no filter); caller filters by provider (below).

### 5. Backend: route by `provider` instead of country
- `api/station/StationApiServiceImpl.java:149-156` `canFetchData`: `switch (station.provider())` with `case HYDRODATEN / VIGICRUES / HVZ_BW` (exhaustive switch keeps the compiler as checklist).
- `source/InputDataFetcherService.java`: replace `filterByCountry(ids, CountryEnum.X)` with `stationIdsOfProvider(Provider.X)` built from `stationDao.getStations()`; French job: `spotDao.getReferencedStationIds()` ∩ stations with `provider == VIGICRUES`; `triggerFrenchFetchForStationAsync` checks the station's provider via `StationDao`/`StationApiService` instead of `getCountry() != FR`. Rename the `country` parameter/logging in `fetchAndWriteToDb` to `provider`.
- `api/spots/SpotsApiService.java:107` `triggerImmediateFrenchFetchIfNeeded`: same provider check via `stationApiService.getStation(stationId).provider()`.
- `api/health/HealthCheckController.java:27`: `new ApiStationId("CH", "2018")`.

### 6. Backend: station & sample fetchers produce the new fields
- `source/swiss/hydrodaten/stations/SwissStationFetchService.java:56-61`: `new ApiStationId("CH", key)`, `Provider.HYDRODATEN`, state `null`, link `STATION_LINK_PREFIX + key`.
- `source/french/vigicrues/stations/FrenchStationFetchService.java:57-62`: `"FR"`, `Provider.VIGICRUES`, link `https://www.vigicrues.gouv.fr/station/ + id`.
- `source/german/bw/stations/DeBwStationFetchService.java:31-37`: `"DE"`, `Provider.HVZ_BW`, state `"Baden-Württemberg"`, link `https://www.hvz.baden-wuerttemberg.de/pegel.html?id= + id`. Add a `protected String fetchHvzBwData()` seam (like `BwSampleFetchServiceImpl.java:68`) for testability.
- Link prefixes: one `public static final String STATION_LINK_PREFIX` per station fetch service (single source of truth — the migration duplicates the literals once, by necessity).
- Sample fetchers that construct ids: `SwissSampleFetchServiceImpl.java:42` → `"CH"`, `BwSampleFetchServiceImpl.java:41` → `"DE"`. Prefer resolving the `ApiStationId` from the passed‑in `Set<ApiStationId>` by externalId where convenient (that's what the Rivermap fetcher will have to do anyway).
- Put the ISO literals in one place, e.g. `com.aa.msw.model.Country` with `CH/FR/DE` string constants, to avoid scattering `"CH"`.

### 7. API mapping
- `api/station/StationApiController.java:32-37`: pass `s.sourceLink()`, `s.state()` into `ApiStation` (generated constructor only takes required fields → use setters/fluent `.sourceLink(..)`).
- Check `api/spots/*` where `ApiSpot.station` is built — same addition.

### 8. Frontend
- `frontend/src/overview/spotlist/spot/Spot.tsx:136-154`: delete `getStationLinkBaseUrl`/`assertUnreachable`; `const stationLinkUrl = spot.station.sourceLink;` render the link `Button` only when defined.
- `frontend/src/spot/MswAddOrEditUtil.tsx:49-60`: `countryEmoji(country: string)` = regional‑indicator conversion of the 2‑letter code (`String.fromCodePoint(...)`), fallback `🌍` for anything not `/^[A-Z]{2}$/`. Drop `CountryEnum` import.
- `SpotsService.ts:78`, `MswAddSpot.tsx:84`, `MswEditSpot.tsx:86`, remaining `MswAddOrEditUtil.tsx` comparisons: unchanged (string equality); remove the two `// TODO: country` comments (`MswAddSpot.tsx:74`, `MswEditSpot.tsx:76`) — resolved by this change.
- `frontend/src/footer/MswFooter.tsx:62` hardcodes "Source: BAFU" — leave for now (attribution per source is a later improvement).

### 9. Tests (guard the refactor — the user's explicit ask)
Update existing: `StationApiServiceImplMock` (new `Station` fields; `Country.CH` → `"CH"`), `PublicSpotListConfiguration`, `SpotsTest`, `SpotDbServiceTest`, `SwissSampleFetchServiceMock`, `SwissForecastFetchServiceMock`, `*FetchIntegrationTest`s (replace `CountryEnum.X` / `apiStationId(Country.X, …)` with string ids).

Add:
- **Station fetch tests, one per provider** (pattern: `BwSampleFetchIntegrationTest` — subclass, override the network seam, load fixture via `TestResourceLoader`):
  - `DeBwStationFetchServiceTest` using existing `testdata/hvz_bw_stations.js`: every station has `country == "DE"`, `provider == HVZ_BW`, `state == "Baden-Württemberg"`, `sourceLink == prefix + externalId`, label `name/river`, coordinates non‑null.
  - `SwissStationFetchServiceTest` with new small fixtures `testdata/hydrodaten_stations.json` + `testdata/existenz_locations.json` (capture 3–5 stations from the live endpoints): `country == "CH"`, `provider == HYDRODATEN`, link, lat/lon joined from existenz.
  - `FrenchStationFetchServiceTest` with `testdata/vigicrues_stations.json` + one `vigicrues_station_detail.json` (override `fetchAsString(url)` to dispatch on URL): `country == "FR"`, `provider == VIGICRUES`, link, Lambert93→WGS84 conversion sane, overseas (`97…`) skipped. Requires making `fetchStationDetails` overridable / adding a seam and skipping the `Thread.sleep` jitter in tests (e.g. protected `delay()` no‑op override).
- **`StationRepositoryTest`** (extends `integrationtest/IntegrationTest`, Testcontainers): persist a `Station` with all fields → `getStations()` round‑trips `provider/state/sourceLink/country`; `deleteByStationId` removes exactly that `(country, externalId)`. This also exercises `V1.30` on a fresh DB.
- **Migration backfill check** (manual, see Verification) — the test DB is always clean‑migrated, so the `DE_BW → DE` / link backfill is verified against a copy of real data instead.
- Extend `SpotsTest` REST assertion to check `station.sourceLink` is present for a public spot.

---

## Verification (step 1)
1. `docker compose up -d` (root) → `./gradlew :backend:generateJooq openApiGenerateSpring openApiGenerateReact` → `./gradlew :backend:build` (compiles; `test` runs Testcontainers incl. `V1.30`).
2. Backfill check on real‑shaped data: point a local backend at a DB dump / the dev DB copy, start it (Flyway applies `V1.30`), then via `http-client/msw/StationsApi.http` `GET /api/v1/stations` — expect no `DE_BW` anywhere, `sourceLink` populated for all stations, `state` only on BW; `SELECT DISTINCT country FROM sample_table` → `CH, FR, DE`. Use `:backend:flywayInfo` (not psql) to inspect migration state.
3. Frontend: `npm start`; open a spot → link icon opens the authority page (CH/FR/DE); add‑spot typeahead shows flags; TypeScript build clean (`CountryEnum` gone).
4. Let the scheduled fetchers run locally once (`0 1/10`, `0 3/5`, `0 5/10`) and confirm new samples still arrive for CH, FR and DE stations (provider routing works) — check logs "Fetching HYDRODATEN data…", etc.
5. Deploy backend + frontend together (API shape changed).

---

## Step 2 — Rivermap stations (follow‑up PR, after step 1 is merged)
- `V1.31__add_rivermap_provider.sql`: `ALTER TYPE provider ADD VALUE 'RIVERMAP';` (own file — new enum values can't be used in the same transaction they're added).
- Config: `rivermap.api-key` in `application.properties` (git‑ignored), `application.properties-TEMPLATE`, `deployment/docker-compose.yml`; bind via `@ConfigurationProperties(prefix="rivermap")` (pattern: `FirebaseInitialize.java:59`).
- `source/AbstractFetchService.java`: add `fetchAsString(String url, Map<String,String> headers)` overload; existing method delegates. Also set connect/read timeouts.
- `source/rivermap/stations/RivermapStationFetchService` (`@Profile("!test")`, seam `protected String fetchRivermapStations()`): `GET {base}/v2/stations?type=online` with `X-Key`. Map: `externalId = id` (UUID), `country = countryCode`, `state = state`, `provider = RIVERMAP`, `label = name + "/" + river` (first available translation, prefer `de`,`en`), `lat/lng = latlng / 1_000_000`, `sourceLink` = first non‑null of `sourceLinks[de|en|first].flow|level` — i.e. the **authority's** page (ag.ch, chcantabrico.es, hnd.bayern.de…), never a rivermap.org URL. **Exclusions** (already covered by our own providers): `countryCode == "FR"`; `countryCode == "DE" && state == "Baden-Württemberg"`; `countryCode == "CH" && any sourceLink contains "hydrodaten.admin.ch"`. Keep only stations whose `sensors` contain `level` or `flow`.
- `StationApiServiceImpl.fetchStations()`: add the Rivermap set; `canFetchData` `case RIVERMAP -> true` for now (list is already active+online; refine in step 3 with a bulk readings check — per‑station readings calls are rate‑limited to ~1/s).
- Tests: `testdata/rivermap_stations.json` = the 5‑station sample from the user → expect **kept**: Etzgen (CH, ag.ch), Ramales (ES), Peißenberg (DE/Bayern); **dropped**: Ocourt (CH hydrodaten), Albbruck (DE/Baden‑Württemberg). Assert mapped fields incl. `latlng` scaling and link selection. `@Profile("test")` mock returning `Set.of()`.
- README: document `rivermap.api-key`.

## Step 3 — Rivermap readings (follow‑up)
- `RivermapSampleFetchService(Impl|Mock)`: one `GET /v2/stations/readings?from=30` per tick (rate limit: bucket 2, refill 1/2 min → schedule every 10 min, e.g. `0 7/10 * * * *`, with its own `AtomicBoolean` in `InputDataFetcherService`). `m3s` → `FLOW`, `cm` → `HEIGHT`, `ts` epoch‑seconds → `OffsetDateTime` UTC. Resolve `ApiStationId` from the known RIVERMAP station set by UUID — unknown station ids are dropped. Persist via `sampleDao.persistSamplesIfNotExist` (`ON CONFLICT DO NOTHING` handles "only new samples"). Note it inserts one statement per sample; keep the window small (`from=30`) so this stays cheap. Then `canFetchData` for RIVERMAP can use the bulk readings response.
