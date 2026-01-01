package com.aa.msw.helper;

import com.aa.msw.database.helpers.id.SpotId;
import com.aa.msw.database.repository.dao.SpotDao;
import com.aa.msw.gen.jooq.enums.Country;
import com.aa.msw.model.Spot;
import com.aa.msw.model.SpotTypeEnum;
import com.aa.msw.source.InputDataFetcherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Set;

import static com.aa.msw.database.helpers.EnumConverterHelper.apiStationId;

@Component
public class PublicSpotListConfiguration {
    public static final Set<Spot> PUBLIC_RIVER_SURF_SPOTS = Set.of();
    public static final Set<Spot> PUBLIC_BUNGEE_SURF_SPOTS = Set.of(
            new Spot(new SpotId(), true, SpotTypeEnum.BUNGEE_SURF, "Zürich", apiStationId(Country.CH, "2243"), 75, 350),
            new Spot(new SpotId(), true, SpotTypeEnum.BUNGEE_SURF, "Bern", apiStationId(Country.CH, "2135"), 80, 360),
            new Spot(new SpotId(), true, SpotTypeEnum.BUNGEE_SURF, "Luzern", apiStationId(Country.CH, "2152"), 80, 350),
            new Spot(new SpotId(), true, SpotTypeEnum.BUNGEE_SURF, "Basel", apiStationId(Country.CH, "2091"), 850, 2500),
            new Spot(new SpotId(), true, SpotTypeEnum.BUNGEE_SURF, "St. Gallen", apiStationId(Country.CH, "2473"), 130, 1300)
    );
    private static final Logger LOG = LoggerFactory.getLogger(PublicSpotListConfiguration.class);
    public final SpotDao spotDao;
    private final InputDataFetcherService inputDataFetcherService;

    public PublicSpotListConfiguration(SpotDao spotDao, InputDataFetcherService inputDataFetcherService) {
        this.spotDao = spotDao;
        this.inputDataFetcherService = inputDataFetcherService;
    }

    @Transactional
    public void persistPublicSpots() {
        if (getPublicSpotsOfType(SpotTypeEnum.RIVER_SURF) != PUBLIC_RIVER_SURF_SPOTS.size() ||
                getPublicSpotsOfType(SpotTypeEnum.BUNGEE_SURF) != PUBLIC_BUNGEE_SURF_SPOTS.size()) {
            LOG.info("PERSISTING PUBLIC SPOTS");
            persistSpots(PUBLIC_RIVER_SURF_SPOTS);
            persistSpots(PUBLIC_BUNGEE_SURF_SPOTS);

            try {
                inputDataFetcherService.fetchDataAndWriteToDb();
            } catch (IOException | URISyntaxException e) {
                // NOP. Should never happen.
                // If something went wrong, the data will get fetched again in a few minutes and the problem fixes itself.
            }
        }
    }

    private double getPublicSpotsOfType(SpotTypeEnum spotType) {
        return spotDao.getPublicSpots().stream()
                .filter(s -> s.type() == spotType)
                .toList()
                .size();
    }

    private void persistSpots(Set<Spot> spots) {
        spots.forEach(spotDao::persist);
    }
}
