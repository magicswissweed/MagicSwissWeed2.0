package com.aa.msw.database.repository.dao;

import com.aa.msw.database.exceptions.NoDataAvailableException;
import com.aa.msw.database.helpers.id.LastFewDaysId;
import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.model.LastFewDays;

import java.util.Set;

public interface LastFewDaysDao extends Dao<LastFewDaysId, LastFewDays> {

    void persistLastFewDaysSamples(Set<LastFewDays> fetchedLastFewDaysSamples);

    LastFewDays getForStation(ApiStationId stationId) throws NoDataAvailableException;
}
