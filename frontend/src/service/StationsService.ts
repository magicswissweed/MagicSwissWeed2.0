import {ApiStation, StationApi} from "../gen/msw-api-ts";
import {AxiosResponse} from "axios";

type SubscriberCallback = (stations: Array<ApiStation>) => void;

class StationsService {
    private stations: Array<ApiStation> = [];
    private subscribers: Array<SubscriberCallback> = [];

    async fetchData() {
        new StationApi().getStations()
            .then(this.writeStationsToState)
    }

    subscribe(callback: SubscriberCallback): void {
        this.subscribers.push(callback);
    }

    unsubscribe(callback: SubscriberCallback): void {
        this.subscribers = this.subscribers.filter((sub) => sub !== callback);
    }

    private writeStationsToState = (res: AxiosResponse<ApiStation[], any>) => {
        if (res && res.data) {
            this.stations = res.data;
            this.notifySubscribers();
        }
    };

    private notifySubscribers(): void {
        this.subscribers.forEach((callback) => callback(this.stations));
    }

    getStations() {
        return this.stations;
    }
}

export const stationsService = new StationsService();
