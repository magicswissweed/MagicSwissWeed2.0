package com.aa.msw.database.helpers;

import com.aa.msw.gen.api.ApiMeasurementType;
import com.aa.msw.gen.api.ApiStationId;
import com.aa.msw.gen.jooq.enums.MeasurementType;

public class EnumConverterHelper {

    static public ApiStationId apiStationId(String country, String stationid) {
        return new ApiStationId(country, stationid);
    }

    static public ApiMeasurementType apiMeasurementType(MeasurementType measurementType) {
        return ApiMeasurementType.fromValue(measurementType.getLiteral());
    }

    static public MeasurementType measurementType(ApiMeasurementType apiMeasurementType) {
        return MeasurementType.valueOf(apiMeasurementType.name());
    }
}
