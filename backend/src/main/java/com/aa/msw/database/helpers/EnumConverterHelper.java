package com.aa.msw.database.helpers;

import com.aa.msw.gen.api.ApiMeasurementType;
import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.gen.api.CountryEnum;
import com.aa.msw.gen.jooq.enums.Country;
import com.aa.msw.gen.jooq.enums.MeasurementType;

public class EnumConverterHelper {

    static public ApiStationId apiStationId(Country country, String stationid) {
        CountryEnum countryEnum = CountryEnum.fromValue(country.name());
        return new ApiStationId(countryEnum, stationid);
    }

    static public Country country(CountryEnum country) {
        return Country.valueOf(country.name());
    }

    static public ApiMeasurementType apiMeasurementType(MeasurementType measurementType) {
        return ApiMeasurementType.fromValue(measurementType.getLiteral());
    }

    static public MeasurementType measurementType(ApiMeasurementType apiMeasurementType) {
        return MeasurementType.valueOf(apiMeasurementType.name());
    }
}
