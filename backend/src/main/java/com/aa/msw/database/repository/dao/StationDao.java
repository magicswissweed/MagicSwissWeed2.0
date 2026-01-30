package com.aa.msw.database.repository.dao;

import com.aa.msw.database.helpers.id.StationId;
import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.model.Station;

import java.util.Set;

public interface StationDao extends Dao<StationId, Station> {
    Set<Station> getStations();

    void deleteByStationId(ApiStationId stationId);
}
