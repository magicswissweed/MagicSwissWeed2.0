package com.aa.msw.api.station;

import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.model.Station;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Set;

public interface StationApiService {

    @Transactional
    Set<Station> getStations();

    @Transactional
    void fetchStationsAndSaveToDb();

    @Transactional
    Station getStation(ApiStationId id) throws NoSuchElementException;
}
