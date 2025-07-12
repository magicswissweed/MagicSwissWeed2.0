package com.aa.msw.database.repository.dao;

import com.aa.msw.database.exceptions.NoDataAvailableException;
import com.aa.msw.database.helpers.id.Last40DaysId;
import com.aa.msw.model.Last40Days;

import java.util.Set;

public interface Last40DaysDao extends Dao<Last40DaysId, Last40Days> {
    void deleteAll();

    void persistLast40DaysSamples(Set<Last40Days> fetchedLast40DaysSamples);

    Last40Days getForStation(Integer stationId) throws NoDataAvailableException;
}
