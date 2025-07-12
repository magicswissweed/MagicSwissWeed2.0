package com.aa.msw.api.graph.last40days;

import com.aa.msw.database.exceptions.NoDataAvailableException;
import com.aa.msw.model.Last40Days;

public interface Last40DaysApiService {
    Last40Days getLast40Days(Integer stationId) throws NoDataAvailableException;
}
