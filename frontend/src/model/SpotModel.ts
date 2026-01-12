import {
    ApiFlowSample,
    ApiFlowStatusEnum,
    ApiForecast,
    ApiHistoricalYears,
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
    minFlow: number;
    maxFlow: number;
    station: ApiStation;
    currentSample: ApiSample;
    flowStatus: FlowColorEnum;
    forecastLoaded: boolean;
    forecast: ApiForecast | undefined;
    lastFewDaysLoaded: boolean;
    lastFewDays: Array<ApiFlowSample> | undefined;
    historical: ApiHistoricalYears | undefined;
    withNotification: boolean;

    constructor(
        id: string,
        name: string,
        stationId: ApiStationId,
        spotType: ApiSpotInformationSpotTypeEnum,
        isPublic: boolean,
        minFlow: number,
        maxFlow: number,
        station: ApiStation,
        currentSample: ApiSample,
        flowStatus: FlowColorEnum,
        forecastLoaded: boolean,
        forecast: ApiForecast | undefined,
        lastFewDaysLoaded: boolean,
        lastFewDays: Array<ApiFlowSample> | undefined,
        historical: ApiHistoricalYears | undefined,
        withNotification: boolean) {
        this.id = id;
        this.name = name;
        this.stationId = stationId;
        this.spotType = spotType;
        this.isPublic = isPublic;
        this.minFlow = minFlow;
        this.maxFlow = maxFlow;
        this.station = station;
        this.currentSample = currentSample;
        this.flowStatus = flowStatus
        this.forecastLoaded = forecastLoaded;
        this.lastFewDaysLoaded = lastFewDaysLoaded;
        this.lastFewDays = lastFewDays;
        this.forecast = forecast;
        this.historical = historical;
        this.withNotification = withNotification;
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
