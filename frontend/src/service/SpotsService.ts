import {
    ApiSpotInformation,
    HistoricalApi,
    SpotsApi,
    StationToApiHistoricalYears
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
                let currentTemperature = s.currentTemperature
                    ? DateTimeConverter.utcApiSampleToLocalTime(s.currentTemperature)
                    : undefined;
                return new SpotModel(
                    s.id,
                    s.name,
                    s.stationId,
                    s.spotType,
                    s.isPublic,
                    s.measurementType,
                    s.minValue,
                    s.maxValue,
                    s.station,
                    currentSample,
                    currentTemperature,
                    getFlowColorEnumFromFlowStatus(s.flowStatusEnum),
                    undefined,
                    s.withNotification,
                    s.dataPending);
            })
            this.setSpots(spots);
        }
    };

    private addHistoricalDataToState(res: AxiosResponse<StationToApiHistoricalYears[], any>) {
        if (res && res.data) {
            let updatedSpots = this.spots.map(s => {
                let filteredListByStationId =
                    res.data.filter(i =>
                        i.station.country == s.stationId.country && i.station.externalId === s.stationId.externalId);
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
