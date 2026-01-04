package com.aa.msw.api.station;

import com.aa.msw.database.helpers.id.StationId;
import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.gen.jooq.enums.Country;
import com.aa.msw.model.Station;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Set;

import static com.aa.msw.database.helpers.EnumConverterHelper.apiStationId;

@Service
class StationApiServiceImplMock implements StationApiService {

    @Override
    public Set<Station> getStations() {
        return Set.of(
                station(Country.CH, "2018", "Station 1"),
                station(Country.CH, "2243", "Station 2"),
                station(Country.CH, "2174", "Station 3"),
                station(Country.CH, "2105", "Station 4"),
                station(Country.CH, "2152", "Station 5"),
                station(Country.CH, "2091", "Station 6"),
                station(Country.CH, "2135", "Station 7"),
                station(Country.CH, "2473", "Station 8"),
                station(Country.FR, "A12345", "Station 9")
        );
    }

    private Station station(Country country, String stationId, String label) {
        return new Station(new StationId(), apiStationId(country, stationId), label, 2.0, 3.0);
    }

    @Override
    public void fetchStationsAndSaveToDb() {

    }

    @Override
    public Station getStation(ApiStationId id) throws NoSuchElementException {
        return getStations().stream()
                .filter(s -> s.stationId().equals(id))
                .findFirst().orElseThrow();
    }
}
