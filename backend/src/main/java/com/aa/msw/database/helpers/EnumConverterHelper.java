package com.aa.msw.database.helpers;

import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.gen.api.CountryEnum;
import com.aa.msw.gen.jooq.enums.Country;

public class EnumConverterHelper {

    static public ApiStationId apiStationId(Country country, String stationid) {
        CountryEnum countryEnum = CountryEnum.fromValue(country.name());
        return new ApiStationId(countryEnum, stationid);
    }

    static public Country country(CountryEnum country) {
        return Country.valueOf(country.name());
    }
}
