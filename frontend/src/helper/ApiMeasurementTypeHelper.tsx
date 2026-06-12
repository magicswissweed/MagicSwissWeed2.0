import {ApiMeasurementType} from "../gen/msw-api-ts";

export function measurementLabel(measurementType?: ApiMeasurementType): string {
    switch (measurementType) {
        case ApiMeasurementType.Height:
            return 'Height';
        case ApiMeasurementType.Temperature:
            return 'Temperature';
        default:
            return 'Flow';
    }
}

export function measurementUnit(measurementType: ApiMeasurementType) {
    switch (measurementType) {
        case ApiMeasurementType.Height:
            return "cm";
        case ApiMeasurementType.Temperature:
            return "°C";
        default:
            return "m³/s";
    }
}
