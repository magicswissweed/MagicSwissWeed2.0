import {ApiMeasurementType} from "../gen/msw-api-ts";

export function measurementLabel(measurementType?: ApiMeasurementType): string {
    return measurementType === ApiMeasurementType.Height ? 'Height' : 'Flow';
}

export function measurementUnit(measurementType: ApiMeasurementType) {
    let flowUnit = "m³/s";
    let heightUnit = "cm";
    return measurementType === ApiMeasurementType.Height ? heightUnit : flowUnit;
}
