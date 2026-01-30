package com.aa.msw.source.french.vigicrues.historical.lastThirty;

import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.model.LastFewDays;
import com.aa.msw.source.french.vigicrues.AbstractFrenchLineFetchService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Set;

@Profile("test")
@Service
public class FrenchLast30DaysSampleFetchServiceMock extends AbstractFrenchLineFetchService implements FrenchLast30DaysSampleFetchService {
    FrenchLast30DaysSampleFetchServiceMock() {
        super("https://www.vigicrues.gouv.fr/services/observations.json/index.php?CdStationHydro=", "&GrdSerie=Q&FormatSortie=simple");
    }

    @Override
    public Set<LastFewDays> fetchLast30DaysSamples(Set<ApiStationId> stationIds) {
        return Set.of();
    }
}
