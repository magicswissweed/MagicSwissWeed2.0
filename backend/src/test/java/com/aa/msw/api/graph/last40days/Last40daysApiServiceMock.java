package com.aa.msw.api.graph.last40days;

import com.aa.msw.model.Last40Days;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile("test")
@Service
class Last40daysApiServiceMock implements Last40DaysApiService {

    @Override
    public Last40Days getLast40Days(Integer stationId) {
        return null;
    }
}
