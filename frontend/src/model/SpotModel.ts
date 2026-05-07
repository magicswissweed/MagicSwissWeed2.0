import {
    ApiFlowSample,
    ApiFlowStatusEnum,
    ApiForecast,
    ApiHistoricalYears,
    ApiMeasurementType,
    ApiSample,
    ApiSpotInformationSpotTypeEnum,
    ApiStation,
    ApiStationId
} from "../gen/msw-api-ts";

export class SpotModel {
    id: string;
    name: string;
    stationId: ApiStationId;
    spotType: ApiSpotInformationSpotTypeEnum;
    isPublic: boolean;
    measurementType: ApiMeasurementType;
    minValue: number;
    maxValue: number;
    station: ApiStation;
    currentSample: ApiSample | undefined;
    flowStatus: FlowColorEnum;
    forecastLoaded: boolean;
    forecast: ApiForecast | undefined;
    lastFewDaysLoaded: boolean;
    lastFewDays: Array<ApiFlowSample> | undefined;
    historical: ApiHistoricalYears | undefined;
    withNotification: boolean;
    dataPending: boolean;

    constructor(
        id: string,
        name: string,
        stationId: ApiStationId,
        spotType: ApiSpotInformationSpotTypeEnum,
        isPublic: boolean,
        measurementType: ApiMeasurementType,
        minValue: number,
        maxValue: number,
        station: ApiStation,
        currentSample: ApiSample | undefined,
        flowStatus: FlowColorEnum,
        forecastLoaded: boolean,
        forecast: ApiForecast | undefined,
        lastFewDaysLoaded: boolean,
        lastFewDays: Array<ApiFlowSample> | undefined,
        historical: ApiHistoricalYears | undefined,
        withNotification: boolean,
        dataPending: boolean) {
        this.id = id;
        this.name = name;
        this.stationId = stationId;
        this.spotType = spotType;
        this.isPublic = isPublic;
        this.measurementType = measurementType;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.station = station;
        this.currentSample = currentSample;
        this.flowStatus = flowStatus
        this.forecastLoaded = forecastLoaded;
        this.lastFewDaysLoaded = lastFewDaysLoaded;
        this.lastFewDays = lastFewDays;
        this.forecast = forecast;
        this.historical = historical;
        this.withNotification = withNotification;
        this.dataPending = dataPending;
    }
}

export enum FlowColorEnum {
    GREEN = "green", ORANGE = "orange", RED = "red"
}

export function getFlowColorEnumFromFlowStatus(apiFlowStatus: ApiFlowStatusEnum): FlowColorEnum {
    switch (apiFlowStatus) {
        case "GOOD":
            return FlowColorEnum.GREEN
        case "TENDENCY_TO_BECOME_GOOD":
            return FlowColorEnum.ORANGE
        case "BAD":
            return FlowColorEnum.RED
    }
}
