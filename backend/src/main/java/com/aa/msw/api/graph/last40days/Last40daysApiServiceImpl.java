package com.aa.msw.api.graph.last40days;

import com.aa.msw.database.exceptions.NoDataAvailableException;
import com.aa.msw.database.repository.dao.Last40DaysDao;
import com.aa.msw.model.Last40Days;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile("!test")
@Service
public class Last40daysApiServiceImpl implements Last40DaysApiService {

    private final Last40DaysDao last40DaysDao;

    public Last40daysApiServiceImpl(Last40DaysDao last40DaysDao) {
        this.last40DaysDao = last40DaysDao;
    }

    @Override
    public Last40Days getLast40Days(Integer stationId) throws NoDataAvailableException {
        return last40DaysDao.getForStation(stationId);
    }

}
