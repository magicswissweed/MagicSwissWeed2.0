# Rivermap integration — Step 1: make station model generic (refactor only)

## Context

We want to import stations from the external [Rivermap API](https://api.rivermap.org/) (`GET /v2/stations`) and later
their readings (`/v2/stations/readings`). Rivermap spans many countries and, per country, multiple states with different
data sources. Our current model can't express that: `CountryEnum` (`CH | FR | DE_BW`) is simultaneously the country, the
data provider, and the key by which the fetchers and the frontend (source link, flag) discriminate stations. `DE_BW`
already shows the enum is really a *provider*, not a country.

Decision (confirmed with the user): **first do a pure refactor** that makes the model generic and migrates existing
data, with tests guarding the station data; **then** add Rivermap (stations, then readings) in follow-up steps.
Rivermap: online stations only; source link served by the backend for all stations; no source *name* for now (link
only).

Branch: `feature/rivermap`. Persistence is jOOQ + Flyway (no JPA); OpenAPI spec
`backend/src/main/resources/api/mswApi.yaml` generates both backend (`openApiGenerateSpring`) and frontend
(`openApiGenerateReact`) clients — always regenerate both and check in the generated code (`backend/README.md`).

### Naming: `provider` vs `source`

- **`provider`** = *which API we call to get a station's data* (hydrodaten/existenz, Vigicrues, HVZ‑BW, Rivermap).
  Internal fetcher discriminator; drives the scheduled jobs and `canFetchData`. Rivermap **is** a provider: for its
  stations we never call the authority ourselves. **Backend‑only**: persisted on `station_table` because the station
  cache is rebuilt from the DB on restart and, with `country` a plain ISO code, nothing else tells the Swiss job to skip
  a Rivermap `CH` station (e.g. Etzgen/ag.ch). Never sent to the frontend — the frontend needs only `id.country`,
  `sourceLink` (and optionally `state`).
- **`source`** = *the actual authority behind the data* (BAFU, Kanton Aargau, CH Cantábrico, HND Bayern…). Represented
  today only by the per‑station **`source_link`** (full URL the user is routed to). Rivermap is a wrapper around many
  sources — it is never a `source`. A `source_name` column can be added later if the UI needs attribution text (Rivermap
  exposes it via `dataSourceId` → `sources[]`).

---

## Status

- **2026-08-27 — Step 1 implemented on `feature/rivermap`** (uncommitted): migration `V1.30`, jOOQ + OpenAPI
  regenerated, backend/frontend refactored, tests added (`GenericCountryMigrationTest` covers the V1.30 backfill on a
  Testcontainers DB starting from V1.29). Full backend test suite green; frontend `tsc` clean.
- Local dev DB `msw` (and the codegen DB `gradlecodegen`, already cleaned) contained a stale, never-committed
  `1.30 add DE BY to country enum` row from 2026-05-14 → the real `V1.30` fails Flyway validation there until that
  history row is removed
  (`DELETE FROM flyway_schema_history WHERE version = '1.30' AND description = 'add DE BY to country enum'`) — the stale
  enum value itself is harmless because `V1.30` drops the type.
- Gradle 8.7 needs a JDK 21 (`JAVA_HOME=$(/usr/libexec/java_home -v 21)`); the default JDK 25 fails.
- Step 1 committed as `567cae4` (user trimmed the added test classes). Deployed: not yet.
- **2026-08-27 — Step 2 implemented** (uncommitted): `V1.31` (+ jOOQ regen), `RivermapConfigProperties`,
  `AbstractFetchService.fetchAsString(url, headers)` + timeouts, `source/rivermap/**` (models, `RivermapStationFilter`,
  `RivermapStationFetchService`), wired into `StationApiServiceImpl` (`case RIVERMAP -> true`), template/README,
  `RivermapStationFetchServiceTest` on the 5-station sample. Local `application.properties` got the key. Server: add
  api-key to docker-compose-template before deploying.
- **2026-08-27 — Step 3 implemented** (uncommitted): `RivermapSampleFetchService` (+`Impl` `!test` / `Mock` `test`), readings models, poll job `0 7/10 * * * *` in `InputDataFetcherService` (`?from=30&to=30`, `m3s→FLOW`, `cm→HEIGHT`, unknown stations dropped, dedup by `persistSamplesIfNotExist`), nightly station validation for RIVERMAP now uses one bulk 6h readings request (`StationApiServiceImpl.fetchRivermapStationsWithReadings`; an empty answer keeps all stations). `RivermapSampleFetchServiceImplTest`.
- **2026-09-03 — station validation fixed**: the readings endpoint answers with the *latest* reading of a station even outside the requested window (live check: 48 of 1507 stations in a 6h request had a newest reading older than 90 days, oldest ~5.6 years) → set membership kept dead gauges. `StationApiServiceImpl.fetchRivermapStationsWithRecentReadings` now only counts stations whose newest reading is younger than `RIVERMAP_MAX_READING_AGE` (3 months); the rest are deleted by the nightly job as before. Guarded by `StationApiServiceImplTest` (Mockito). Known leftover for all providers: stations that vanish from a provider's list are never deleted (the nightly job only adds/validates fetched stations).
- Open: French duplicates via `hydro.eaufrance.fr` (18 of 37 FR stations are Vigicrues gauges) — decide accept / FR rule / code-based dedup. Server: `RIVERMAP_API_KEY`.

---

## Target model (after step 1)

| Concept                             | Before                      | After                                                                                                                                                                                                                     |
|-------------------------------------|-----------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `ApiStationId.country`              | `CountryEnum` (CH/FR/DE_BW) | **plain `string`, ISO‑3166‑1 alpha‑2** (`CH`, `FR`, `DE`, …). Business key stays `(country, externalId)`.                                                                                                                 |
| DB `country` column (5 tables)      | PG enum `country`           | `VARCHAR`; `DE_BW` rows → `DE`                                                                                                                                                                                            |
| Data provider                       | implicit in `CountryEnum`   | new PG enum `provider` + `station_table.provider NOT NULL` = `HYDRODATEN \| VIGICRUES \| HVZ_BW` (step 2 adds `RIVERMAP`). Used **only** on `station_table`; fetchers resolve it via the station. Not exposed in the API. |
| State/region                        | –                           | `station_table.state VARCHAR NULL` (`Baden-Württemberg` for HVZ_BW, null otherwise)                                                                                                                                       |
| Link to the source (authority) page | built in frontend `switch`  | `station_table.source_link VARCHAR NULL` = **full URL incl. externalId**, exposed as optional `ApiStation.sourceLink`; frontend just renders it                                                                           |
| Flag                                | frontend `switch` on enum   | computed generically from ISO code (`🇨🇭` from `CH`)                                                                                                                                                                       |

Key-collision note: `(country, externalId)` remains the identity. `unique_sample` already omits `country` (`V1.24`), so
the system de facto relies on externalIds being distinct across providers — true today (numeric CH, alnum FR,
zero‑padded BW, UUIDs for Rivermap).

**Deferred (decided 2026-08-27, "leave for the moment"):** `(country, externalId)` is unique only by the disjoint id
formats, not by design — the namespace owner is the *provider*. If a second direct provider in an already covered
country appears, do the follow-up: natural key `(provider, externalId)` (replace `country` by `provider` in
sample/forecast/spot/historical tables, `ApiStationId = {provider, externalId}`, country becomes a station attribute).
Cheap hardening available any time: `UNIQUE (country, stationid)` on `station_table`, add `country` to `unique_sample`/
`unique_forecast` (the lookup index already has these columns) + matching `onConflict(...)` columns in the repositories.
Do **not** reference `db_id` instead: the nightly job deletes and re-inserts stations, so `db_id` is not stable.

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

Notes: `sample_table_lookup_idx (country, …)` is rebuilt automatically by the type change; `sample_table` is the big one
(300‑day retention) → the migration rewrites it once; plan a short deploy window. `CREATE TYPE` + use in same
transaction is fine (only `ADD VALUE` has that restriction).

**Lesson from the first dev deploy (2026-08-27):** the original V1.30 did the `DE_BW → DE` mapping as a separate `UPDATE sample_table … WHERE country = 'DE_BW'` *after* the type change — millions of BW rows rewritten a second time with random index IO; after 23 minutes it was still running on the dev server (the backend does not open its port until Flyway finishes → Apache 502). Rewritten before it was recorded anywhere: the mapping now happens inside the type conversion (`USING CASE WHEN country = 'DE_BW' THEN 'DE' ELSE country::text END`), one sequential rewrite per table, no `UPDATE`s. Guarded by `GenericCountryMigrationTest` (Testcontainers, migrates to 1.29, inserts old‑style rows, migrates to latest).

### 2. Regenerate jOOQ

`./gradlew :backend:generateJooq` (needs the root `docker-compose.yml` DB running; task depends on `flywayMigrate`
against `gradlecodegen`). Generated `com.aa.msw.gen.jooq.enums.Country` disappears, `…enums.Provider` appears,
`StationTable` gains `PROVIDER`, `STATE`, `SOURCE_LINK`. Compile errors then enumerate every site to fix.

### 3. OpenAPI `mswApi.yaml` + regenerate both clients

- Delete `CountryEnum` schema; `ApiStationId.country` → `type: string` (description: ISO 3166‑1 alpha‑2).
- `ApiStation`: add optional `sourceLink: string` and `state: string` (not in `required`).
- Fix stale summary "from BAFU" on `/api/v1/stations` while there.
- Run `openApiGenerateSpring` **and** `openApiGenerateReact`, commit generated code (`backend/src/generated/api`,
  `frontend/src/gen/msw-api-ts`).

### 4. Backend domain & repositories

- `model/Station.java`: add `Provider provider`, `String state`, `String sourceLink` (use the jOOQ‑generated
  `com.aa.msw.gen.jooq.enums.Provider` directly; test mocks already import jOOQ enums).
- `database/helpers/EnumConverterHelper.java`: `apiStationId(String country, String stationid)`; delete
  `country(CountryEnum)`.
- `database/repository/StationRepository.java`: map the 3 new columns in `mapRecord`/`mapDomain`/`mapEntity`;
  `deleteByStationId` compares `TABLE.COUNTRY.eq(stationId.getCountry())`.
- `SampleRepository`, `ForecastRepository`, `SpotRepository`, `HistoricalYearsDataRepository`: drop `country(...)`
  wrapping — `TABLE.COUNTRY` is now a `String` field (`SampleRepository.java:44,54,66,76,88,133,137`;
  `ForecastRepository.java:62,95,112,156`; `SpotRepository.java:44,57,73,109,118-120`;
  `HistoricalYearsDataRepository.java:84,99,115`).
- `SpotDao.getReferencedStationIds(CountryEnum)` → `getReferencedStationIds()` (no filter); caller filters by provider
  (below).

### 5. Backend: route by `provider` instead of country

- `api/station/StationApiServiceImpl.java:149-156` `canFetchData`: `switch (station.provider())` with
  `case HYDRODATEN / VIGICRUES / HVZ_BW` (exhaustive switch keeps the compiler as checklist).
- `source/InputDataFetcherService.java`: replace `filterByCountry(ids, CountryEnum.X)` with
  `stationIdsOfProvider(Provider.X)` built from `stationDao.getStations()`; French job:
  `spotDao.getReferencedStationIds()` ∩ stations with `provider == VIGICRUES`; `triggerFrenchFetchForStationAsync`
  checks the station's provider via `StationDao`/`StationApiService` instead of `getCountry() != FR`. Rename the
  `country` parameter/logging in `fetchAndWriteToDb` to `provider`.
- `api/spots/SpotsApiService.java:107` `triggerImmediateFrenchFetchIfNeeded`: same provider check via
  `stationApiService.getStation(stationId).provider()`.
- `api/health/HealthCheckController.java:27`: `new ApiStationId("CH", "2018")`.

### 6. Backend: station & sample fetchers produce the new fields

- `source/swiss/hydrodaten/stations/SwissStationFetchService.java:56-61`: `new ApiStationId("CH", key)`,
  `Provider.HYDRODATEN`, state `null`, link `STATION_LINK_PREFIX + key`.
- `source/french/vigicrues/stations/FrenchStationFetchService.java:57-62`: `"FR"`, `Provider.VIGICRUES`, link
  `https://www.vigicrues.gouv.fr/station/ + id`.
- `source/german/bw/stations/DeBwStationFetchService.java:31-37`: `"DE"`, `Provider.HVZ_BW`, state
  `"Baden-Württemberg"`, link `https://www.hvz.baden-wuerttemberg.de/pegel.html?id= + id`. Add a
  `protected String fetchHvzBwData()` seam (like `BwSampleFetchServiceImpl.java:68`) for testability.
- Link prefixes: one `public static final String STATION_LINK_PREFIX` per station fetch service (single source of
  truth — the migration duplicates the literals once, by necessity).
- Sample fetchers that construct ids: `SwissSampleFetchServiceImpl.java:42` → `"CH"`,
  `BwSampleFetchServiceImpl.java:41` → `"DE"`. Prefer resolving the `ApiStationId` from the passed‑in
  `Set<ApiStationId>` by externalId where convenient (that's what the Rivermap fetcher will have to do anyway).
- Put the ISO literals in one place, e.g. `com.aa.msw.model.Country` with `CH/FR/DE` string constants, to avoid
  scattering `"CH"`.

### 7. API mapping

- `api/station/StationApiController.java:32-37`: pass `s.sourceLink()`, `s.state()` into `ApiStation` (generated
  constructor only takes required fields → use setters/fluent `.sourceLink(..)`).
- Check `api/spots/*` where `ApiSpot.station` is built — same addition.

### 8. Frontend

- `frontend/src/overview/spotlist/spot/Spot.tsx:136-154`: delete `getStationLinkBaseUrl`/`assertUnreachable`;
  `const stationLinkUrl = spot.station.sourceLink;` render the link `Button` only when defined.
- `frontend/src/spot/MswAddOrEditUtil.tsx:49-60`: `countryEmoji(country: string)` = regional‑indicator conversion of the
  2‑letter code (`String.fromCodePoint(...)`), fallback `🌍` for anything not `/^[A-Z]{2}$/`. Drop `CountryEnum` import.
- `SpotsService.ts:78`, `MswAddSpot.tsx:84`, `MswEditSpot.tsx:86`, remaining `MswAddOrEditUtil.tsx` comparisons:
  unchanged (string equality); remove the two `// TODO: country` comments (`MswAddSpot.tsx:74`, `MswEditSpot.tsx:76`) —
  resolved by this change.
- `frontend/src/footer/MswFooter.tsx:62` hardcodes "Source: BAFU" — leave for now (attribution per source is a later
  improvement).

### 9. Tests (guard the refactor — the user's explicit ask)

Update existing: `StationApiServiceImplMock` (new `Station` fields; `Country.CH` → `"CH"`),
`PublicSpotListConfiguration`, `SpotsTest`, `SpotDbServiceTest`, `SwissSampleFetchServiceMock`,
`SwissForecastFetchServiceMock`, `*FetchIntegrationTest`s (replace `CountryEnum.X` / `apiStationId(Country.X, …)` with
string ids).

Add:

- **Station fetch tests, one per provider** (pattern: `BwSampleFetchIntegrationTest` — subclass, override the network
  seam, load fixture via `TestResourceLoader`):
- **`StationRepositoryTest`** (extends `integrationtest/IntegrationTest`, Testcontainers): persist a `Station` with all
  fields → `getStations()` round‑trips `provider/state/sourceLink/country`; `deleteByStationId` removes exactly that
  `(country, externalId)`. This also exercises `V1.30` on a fresh DB.
- **Migration backfill check** (manual, see Verification) — the test DB is always clean‑migrated, so the `DE_BW → DE` /
  link backfill is verified against a copy of real data instead.
- Extend `SpotsTest` REST assertion to check `station.sourceLink` is present for a public spot.

---

## Verification (step 1)

1. `docker compose up -d` (root) → `./gradlew :backend:generateJooq openApiGenerateSpring openApiGenerateReact` →
   `./gradlew :backend:build` (compiles; `test` runs Testcontainers incl. `V1.30`).
2. Backfill check on real‑shaped data: point a local backend at a DB dump / the dev DB copy, start it (Flyway applies
   `V1.30`), then via `http-client/msw/StationsApi.http` `GET /api/v1/stations` — expect no `DE_BW` anywhere,
   `sourceLink` populated for all stations, `state` only on BW; `SELECT DISTINCT country FROM sample_table` →
   `CH, FR, DE`. Use `:backend:flywayInfo` (not psql) to inspect migration state.
3. Frontend: `npm start`; open a spot → link icon opens the authority page (CH/FR/DE); add‑spot typeahead shows flags;
   TypeScript build clean (`CountryEnum` gone).
4. Let the scheduled fetchers run locally once (`0 1/10`, `0 3/5`, `0 5/10`) and confirm new samples still arrive for
   CH, FR and DE stations (provider routing works) — check logs "Fetching HYDRODATEN data…", etc.
5. Deploy backend + frontend together (API shape changed).

---

## Step 2 — Rivermap stations (own PR; readings follow in step 3)

Goal: the nightly station job also imports Rivermap's **online** stations that none of our own providers cover, with the
authority's page as `sourceLink`. No readings yet.

### Decision to take before deploying step 2

Stations without samples have `supportedMeasurements = []`; the add‑spot dialog then falls back to `FLOW`
(`MswAddOrEditUtil.tsx:21`) and a spot on such a station shows the "fetching data" placeholder (`dataPending`) until
step 3 delivers readings. Either accept that for the short gap, or **deploy step 2 together with step 3** (recommended).
Implementing step 2 on its own is fine either way.

### 2.1 Migration `V1.31__add_rivermap_provider.sql`

```sql
ALTER TYPE provider ADD VALUE 'RIVERMAP';
```

Own file on purpose: a value added with `ADD VALUE` can't be used inside the same transaction. Then
`./gradlew :backend:generateJooq` (JDK 21, docker DB up) → `Provider.RIVERMAP` exists and the `switch(provider)` in
`StationApiServiceImpl.canFetchData` stops compiling until handled.

### 2.2 Configuration — `rivermap.api-key`

- `backend/.../source/rivermap/RivermapConfigProperties.java`:
  `@Component @ConfigurationProperties(prefix = "rivermap") @Data` with `private String apiKey;` (same pattern as
  `FirebaseInitialize.FirebaseConfigProperties`, `FirebaseInitialize.java:59`). Relaxed binding accepts
  `rivermap.api-key` in properties.
- Add `rivermap.api-key=<FROM RIVERMAP>` to `application.properties-TEMPLATE` and to your local
  `application.properties`; document it in `README.md` §1 (backend secrets). On the server: add the apiKey to the
  docker-compose-template of the backend service in `/opt/ponte-services/magicswissweed` compose (same place as the
  firebase vars) **before** deploying.
- Missing/blank key must not break startup: the fetcher logs a warning and returns an empty set (dev machines without a
  key keep working).

### 2.3 HTTP with headers — `AbstractFetchService`

Add `protected String fetchAsString(String url, Map<String, String> headers)`; the existing `fetchAsString(url)`
delegates with `Map.of()`. Set `conn.setRequestProperty` for each header. While there: `setConnectTimeout(10_000)` /
`setReadTimeout(30_000)` (today there are none). Non‑200 still throws → caller catches → empty set + log.

### 2.4 Package `com.aa.msw.source.rivermap`

- `model/RivermapStationsResponse(List<RivermapStation> stations)` — parse with `FAIL_ON_UNKNOWN_PROPERTIES=false`
  (ignores `sources`, `rivers`, `license`, `elapsedMs`).
-
`model/RivermapStation(String id, String type, List<String> sensors, boolean isActive, String name, Map<String,String> river, String countryCode, String state, List<Long> latlng, Map<String, Map<String,String>> sourceLinks)`.
- `stations/RivermapStationFetchService` (`@Service @Profile("!test")`, extends `AbstractFetchService`):
    - `GET https://api.rivermap.org/v2/stations?type=online` with header `X-Key: <apiKey>`; seam
      `protected String fetchRivermapStations()` for tests.
    - Keep a station only if **all** hold: `type == "online"`, `isActive`, `sensors` contains `level` or `flow`,
      `latlng` has 2 values, and it is **not already fetched by one of our own providers**. Rivermap's UUIDs can't be
      matched against our externalIds, so the authority is recognised via the host of its `sourceLinks` (decided
      2026-08-27, domain‑only variant):
        - skip if any `sourceLinks.*.*` URL has host `hydrodaten.admin.ch` (BAFU → our HYDRODATEN), `vigicrues.gouv.fr`
          (→ VIGICRUES) or `hvz.baden-wuerttemberg.de` (→ HVZ_BW). One constant list in `RivermapStationFilter`; a
          future direct provider = one more domain.
        - No country/state rules: they over‑exclude gauges of *other* authorities in the same region (sample: *Albbruck
          by Swiss Canoe*, DE/Baden‑Württemberg but `datacake.de` — HVZ doesn't have it, so we want it). Cantonal CH
          stations (Etzgen / ag.ch) stay for the same reason.
    - **Plus the FR country rule** (decided 2026-08-27 after the live check): Rivermap lists French gauges via Hydroportail (`hydro.eaufrance.fr`) and 18 of those 37 are the same gauges we have from Vigicrues → all `countryCode == "FR"` stations are skipped (`COUNTRIES_COVERED_BY_OWN_PROVIDERS`), the 19 Hydroportail‑only gauges are given up. No migration needed (never deployed); duplicates already in the dev DB deleted by hand. Remaining accepted leak: a BAFU/HVZ station listed without a link to its domain would appear twice (cosmetic); exact fallback: skip by the authorities' `dataSourceId`.
    - Mapping → `Station`: `stationId = (countryCode, id)` (Rivermap UUID as externalId), `label = name + "/" + river`
      (river: prefer `de`, `en`, then first value; mirrors the BW label), `latitude = latlng[0] / 1_000_000d`,
      `longitude = latlng[1] / 1_000_000d`, `provider = RIVERMAP`, `state = state` (blank → null), `sourceLink` = first
      non‑blank of `sourceLinks[lang].flow`, then `.level`, trying `de, en, fr, it, es`, then any remaining language;
      none → `null` (frontend hides the link icon). Always the authority's URL, never rivermap.org.
    - Any exception → log + `Collections.emptySet()` (same contract as the other station fetchers).

### 2.5 Wire it in — `StationApiServiceImpl`

- Inject `RivermapStationFetchService`; `fetchStations()` adds its result after the three existing providers.
- `canFetchData`: `case RIVERMAP -> true` with a comment: the list is already filtered to active + online, and a
  per‑station readings probe would cost one API call per station (rate limit ~1/s). Step 3 replaces this with one bulk
  `/stations/readings` check.
- Consequence: Rivermap stations are never auto‑deleted by `processFetchedStations` until step 3 — acceptable.
- Rate limit of `/stations`: bucket 2, refill **1 per hour**. The nightly job calls it once; the lazy bootstrap in
  `getStations()` calls it only on an empty DB. Restarting a dev backend with an empty DB more than twice an hour yields
  HTTP 429 → empty Rivermap set until the next run (logged, harmless).

### 2.6 Tests (kept lean)

- `RivermapStationFetchServiceTest` with fixture `testdata/rivermap_stations.json` = the 5‑station sample response
  (subclass, override `fetchRivermapStations()`):
    1. kept = Etzgen (CH/ag.ch), Ramales (ES), Peißenberg (DE/Bayern), Albbruck (DE/BW but `datacake.de`, level only);
       dropped = Ocourt (CH/hydrodaten link).
    2. mapping of Ramales: country `ES`, externalId `e5b917e0-…`, provider `RIVERMAP`, state `Asón`, lat `43.263805`,
       lng `-3.461539`, sourceLink `https://www.chcantabrico.es/…cod_estacion=A141`, label `Ramales/Asón`.
- No Spring mock needed: the fetcher is `@Profile("!test")` and only `StationApiServiceImpl` (also `!test`) uses it.

### 2.7 Frontend

Nothing required: flag comes from the ISO code, link from `sourceLink`. Optional nicety: show `state` after the label in
the typeahead (`MswAddOrEditUtil.tsx:146-156`) so "Ramales/Asón (Asón)" is distinguishable — skip unless wanted.

### 2.8 Verification

1. `./gradlew :backend:build` (JDK 21) — compile + tests.
2. Local backend with a real key: trigger `fetchStationsAndSaveToDb` (restart on a DB whose `station_table` is empty, or
   call it once from a temporary endpoint/test) → `GET /api/v1/stations` via `http-client/msw/StationsApi.http`:
   Rivermap stations present, none with a `hydrodaten.admin.ch` / `vigicrues.gouv.fr` / `hvz.baden-wuerttemberg.de`
   link; spot‑check Etzgen (`CH`, link `ag.ch`), a `ES` station, a `DE`/Bayern station, Albbruck (`DE`/BW,
   `datacake.de`). Eyeball the list for obvious duplicates of known BAFU/Vigicrues/HVZ gauges (the accepted leak).
3. Frontend add‑spot: Rivermap stations appear with 🇪🇸 / 🇦🇹 … flags and on the map; spot link icon opens the authority
   page.
4. Logs after the scheduled sample jobs: HYDRODATEN / VIGICRUES / HVZ_BW counts unchanged (Rivermap stations must not
   leak into them).

## Step 3 — Rivermap readings (follow‑up)

- `RivermapSampleFetchService(Impl|Mock)`: one `GET /v2/stations/readings?from=30` per tick (rate limit: bucket 2,
  refill 1/2 min → schedule every 10 min, e.g. `0 7/10 * * * *`, with its own `AtomicBoolean` in
  `InputDataFetcherService`). `m3s` → `FLOW`, `cm` → `HEIGHT`, `ts` epoch‑seconds → `OffsetDateTime` UTC. Resolve
  `ApiStationId` from the known RIVERMAP station set by UUID — unknown station ids are dropped. Persist via
  `sampleDao.persistSamplesIfNotExist` (`ON CONFLICT DO NOTHING` handles "only new samples"). Note it inserts one
  statement per sample; keep the window small (`from=30`) so this stays cheap. Then `canFetchData` for RIVERMAP can use
  the bulk readings response.
