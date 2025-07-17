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
import {DateTimeConverter} from "./DateTimeConverter";

type SubscriberCallback = (spots: Array<SpotModel>) => void;

// Info: All Timestamps are sent in UTC and are converted to local time before saving them to the SpotModel
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
            let spots: Array<SpotModel> = res.data.map(s => {
                let currentSample = DateTimeConverter.utcApiSampleToLocalTime(s.currentSample);
                return new SpotModel(
                    s.id,
                    s.name,
                    s.stationId,
                    s.spotType,
                    s.isPublic,
                    s.minFlow,
                    s.maxFlow,
                    s.station,
                    currentSample,
                    getFlowColorEnumFromFlowStatus(s.flowStatusEnum),
                    false,
                    undefined,
                    false,
                    undefined,
                    undefined);
            })
            this.setSpots(spots);
        }
    };

    private addLast40DaysToState(res: AxiosResponse<StationToLast40Days[], any>) {
        if (res && res.data) {
            const updatedSpots = this.spots.map(s => {
                const filteredListByStationId = res.data.filter(i => i.station === s.stationId);
                const newLast40Days = DateTimeConverter.utcLast40DaysToLocalTime(filteredListByStationId[0]?.last40Days)
                // create new SpotModel so that react can see that something changed (updating a field is not enough)
                return {
                    ...s,
                    last40DaysLoaded: true,
                    last40Days: newLast40Days,
                }
            });

            this.setSpots(updatedSpots);
        }
    }

    private addForecastsToState(res: AxiosResponse<StationToApiForecasts[], any>) {
        if (res && res.data) {
            const updatedSpots = this.spots.map(s => {
                const filteredListByStationId = res.data.filter(i => i.station === s.stationId);
                const newForecast = DateTimeConverter.utcForecastToLocalTime(filteredListByStationId[0]?.forecast);
                // create new SpotModel so that react can see that something changed (updating a field is not enough)
                return {
                    ...s,
                    forecastLoaded: true,
                    forecast: newForecast,
                }
            });

            this.setSpots(updatedSpots);
        }
    }

    private addHistoricalDataToState(res: AxiosResponse<StationToApiHistoricalYears[], any>) {
        if (res && res.data) {
            let updatedSpots = this.spots.map(s => {
                let filteredListByStationId =
                    res.data.filter(i => i.station === s.stationId);
                const historical = DateTimeConverter.utcHistoricalToLocalTime(filteredListByStationId[0]?.historical);
                // create new SpotModel so that react can see that something changed (updating a field is not enough)
                return {
                    ...s,
                    historical: historical,
                }
            });

            this.setSpots(updatedSpots);
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
