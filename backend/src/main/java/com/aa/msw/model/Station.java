package com.aa.msw.model;

import com.aa.msw.database.helpers.id.HasId;
import com.aa.msw.database.helpers.id.StationId;
import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.gen.jooq.enums.Provider;

/**
 * @param databaseId the surrogate key in our db
 * @param stationId  the business key: ISO country code + the id of the station at its provider
 * @param provider   which API we fetch the data of this station from (backend-only, never exposed to the frontend)
 * @param state      state / region the station is located in, if known
 * @param sourceLink full URL to the station page of the data source (the authority publishing the data), if known
 */
public record Station(
        StationId databaseId,
        ApiStationId stationId,
        String label,
        Double latitude,
        Double longitude,
        Provider provider,
        String state,
        String sourceLink) implements HasId<StationId> {
    @Override
    public StationId getId() {
        return databaseId;
    }
}
