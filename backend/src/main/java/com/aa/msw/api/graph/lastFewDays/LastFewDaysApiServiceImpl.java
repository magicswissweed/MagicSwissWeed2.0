package com.aa.msw.api.graph.lastFewDays;

import com.aa.msw.database.exceptions.NoDataAvailableException;
import com.aa.msw.database.repository.dao.LastFewDaysDao;
import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.model.LastFewDays;
import org.springframework.stereotype.Service;

@Service
public class LastFewDaysApiServiceImpl implements LastFewDaysApiService {

    private final LastFewDaysDao lastFewDaysDao;

    public LastFewDaysApiServiceImpl(LastFewDaysDao lastFewDaysDao) {
        this.lastFewDaysDao = lastFewDaysDao;
    }

    @Override
    public LastFewDays getLastFewDays(ApiStationId stationId) throws NoDataAvailableException {
        return lastFewDaysDao.getForStation(stationId);
    }

}
