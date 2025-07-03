import {
    ApiFlowSample,
    ApiFlowStatusEnum,
    ApiForecast,
    ApiHistoricalYears,
    ApiSample,
    ApiSpotInformationSpotTypeEnum,
    ApiStation
} from "../gen/msw-api-ts";

export class SpotModel {
    id: string;
    name: string;
    stationId: number;
    spotType: ApiSpotInformationSpotTypeEnum;
    isPublic: boolean;
    minFlow: number;
    maxFlow: number;
    station: ApiStation;
    currentSample: ApiSample;
    flowStatus: FlowColorEnum;
    forecastLoaded: boolean;
    forecast: ApiForecast | undefined;
    last40DaysLoaded: boolean;
    last40Days: Array<ApiFlowSample> | undefined;
    historical: ApiHistoricalYears | undefined;

    constructor(
        id: string,
        name: string,
        stationId: number,
        spotType: ApiSpotInformationSpotTypeEnum,
        isPublic: boolean,
        minFlow: number,
        maxFlow: number,
        station: ApiStation,
        currentSample: ApiSample,
        flowStatus: FlowColorEnum,
        forecastLoaded: boolean,
        forecast: ApiForecast | undefined,
        last40DaysLoaded: boolean,
        last40Days: Array<ApiFlowSample> | undefined,
        historical: ApiHistoricalYears | undefined) {
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
        this.last40DaysLoaded = last40DaysLoaded;
        this.last40Days = last40Days;
        this.forecast = forecast;
        this.historical = historical;
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
