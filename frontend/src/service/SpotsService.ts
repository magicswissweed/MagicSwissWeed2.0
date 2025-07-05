import {
    ApiSpotInformation,
    ForecastApi,
    HistoricalApi,
    SampleApi,
    SpotsApi,
    StationToApiForecasts,
    StationToApiHistoricalYears,
    StationToLast40Days
} from "../gen/msw-api-ts";
import {AxiosResponse} from "axios";
import {authConfiguration} from "../api/config/AuthConfiguration";
import {getFlowColorEnumFromFlowStatus, SpotModel} from "../model/SpotModel";

type SubscriberCallback = (spots: Array<SpotModel>) => void;

class SpotsService {
    private spots: Array<SpotModel> = [];
    private subscribers: Array<SubscriberCallback> = [];

    async fetchData(token: any) {
        let config = await authConfiguration(token);
        // Defined here is the order in which our api-calls are executed.
        // First load the most relevant info (fast)
        // -> then load additional infos (less important) -> shown to user as soon as it's loaded.
        new SpotsApi(config).getSpots()
            .then(this.writeSpotsToState)
            .then(() =>
                new ForecastApi(config).getForecasts()
                    .then(this.addForecastsToState.bind(this))
            )
            .then(() => {
                let stationsWithoutForecast = this.spots
                    .filter(s => !s.forecast)
                    .map(s => s.stationId)
                if (stationsWithoutForecast.length > 0) {
                    new SampleApi().getLast40DaysSamples(stationsWithoutForecast)
                        .then(this.addLast40DaysToState.bind(this))
                }
            })
            .then(() =>
                new HistoricalApi(config).getHistoricalData()
                    .then(this.addHistoricalDataToState.bind(this))
            )
    }

    deleteSpot(id: string): void {
        this.spots = this.spots.filter((spot) => spot.id !== id);
        this.notifySubscribers();
    }

    subscribe(callback: SubscriberCallback): void {
        this.subscribers.push(callback);
    }

    unsubscribe(callback: SubscriberCallback): void {
        this.subscribers = this.subscribers.filter((sub) => sub !== callback);
    }

    private writeSpotsToState = (res: AxiosResponse<ApiSpotInformation[], any>) => {
        if (res && res.data) {
            let spots: Array<SpotModel> = res.data.map(s =>
                new SpotModel(
                    s.id,
                    s.name,
                    s.stationId,
                    s.spotType,
                    s.isPublic,
                    s.minFlow,
                    s.maxFlow,
                    s.station,
                    s.currentSample,
                    getFlowColorEnumFromFlowStatus(s.flowStatusEnum),
                    false,
                    undefined,
                    false,
                    undefined,
                    undefined))
            this.setSpots(spots);
        }
    };

    private addLast40DaysToState(res: AxiosResponse<StationToLast40Days[], any>) {
        if (res && res.data) {
            const updatedSpots = this.spots.map(s => {
                const filteredListByStationId = res.data.filter(i => i.station === s.stationId);
                const newLast40Days = filteredListByStationId[0]?.last40Days
                // create new SpotModel so that react can see that something changed (updating a field is not enough)
                return new SpotModel(
                    s.id,
                    s.name,
                    s.stationId,
                    s.spotType,
                    s.isPublic,
                    s.minFlow,
                    s.maxFlow,
                    s.station,
                    s.currentSample,
                    s.flowStatus,
                    s.forecastLoaded,
                    s.forecast,
                    true,
                    newLast40Days,
                    s.historical
                );
            });
            this.setSpots(updatedSpots);
        }
    }

    private addForecastsToState(res: AxiosResponse<StationToApiForecasts[], any>) {
        if (res && res.data) {
            const updatedSpots = this.spots.map(s => {
                const filteredListByStationId = res.data.filter(i => i.station === s.stationId);
                const newForecast = filteredListByStationId[0]?.forecast;
                // create new SpotModel so that react can see that something changed (updating a field is not enough)
                return new SpotModel(
                    s.id,
                    s.name,
                    s.stationId,
                    s.spotType,
                    s.isPublic,
                    s.minFlow,
                    s.maxFlow,
                    s.station,
                    s.currentSample,
                    s.flowStatus,
                    true,
                    newForecast,
                    s.last40DaysLoaded,
                    s.last40Days,
                    s.historical
                );
            });

            this.setSpots(updatedSpots);
        }
    }

    private addHistoricalDataToState(res: AxiosResponse<StationToApiHistoricalYears[], any>) {
        if (res && res.data) {
            let spots = this.spots;
            spots.forEach(s => {
                    let filteredListByStationId =
                        res.data.filter(i => i.station === s.stationId);
                    s.historical = filteredListByStationId[0]?.historical;
                }
            )
            this.setSpots(spots);
        }
    }

    private setSpots(spots: Array<SpotModel>): void {
        this.spots = spots;
        this.notifySubscribers();
    }

    private notifySubscribers(): void {
        this.subscribers.forEach((callback) => callback(this.spots));
    }
}

export const spotsService = new SpotsService();
