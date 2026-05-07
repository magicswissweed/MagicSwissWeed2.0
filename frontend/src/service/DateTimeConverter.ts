import {ApiForecast, ApiHistoricalYears, ApiLineEntry, ApiSample} from "../gen/msw-api-ts";
import {DateTime} from "luxon";

export class DateTimeConverter {
    public static utcForecastToLocalTime(forecast: ApiForecast | undefined): ApiForecast | undefined {
        if (forecast === undefined) {
            return undefined;
        }
        return {
            ...forecast,
            measuredData: this.utcApiLineEntryArrayToLocalTime(forecast.measuredData),
            median: this.utcApiLineEntryArrayToLocalTime(forecast.median),
            twentyFivePercentile: this.utcApiLineEntryArrayToLocalTime(forecast.twentyFivePercentile),
            seventyFivePercentile: this.utcApiLineEntryArrayToLocalTime(forecast.seventyFivePercentile),
            min: this.utcApiLineEntryArrayToLocalTime(forecast.min),
            max: this.utcApiLineEntryArrayToLocalTime(forecast.max)
        }
    }

    public static utcLastFewDaysToLocalTime(lastFewDays: Array<ApiSample> | undefined): Array<ApiSample> | undefined {
        if (lastFewDays === undefined || lastFewDays === null || lastFewDays.length === 0) {
            return lastFewDays;
        }
        return this.utcApiSampleArrayToLocalTime(lastFewDays);
    }

    public static utcHistoricalToLocalTime(historical: ApiHistoricalYears | undefined): ApiHistoricalYears | undefined {
        if (historical === undefined || historical === null) {
            return historical;
        }
        return {
            ...historical,
            median: this.utcApiLineEntryArrayToLocalTime(historical.median),
            twentyFivePercentile: this.utcApiLineEntryArrayToLocalTime(historical.twentyFivePercentile),
            seventyFivePercentile: this.utcApiLineEntryArrayToLocalTime(historical.seventyFivePercentile),
            min: this.utcApiLineEntryArrayToLocalTime(historical.min),
            max: this.utcApiLineEntryArrayToLocalTime(historical.max),
            currentYear: this.utcApiLineEntryArrayToLocalTime(historical.currentYear),
        };
    }

    public static utcApiSampleToLocalTime(sample: ApiSample | undefined): ApiSample | undefined {
        if (sample === undefined || sample === null) {
            return undefined;
        }
        return {
            ...sample,
            timestamp: this.utcToLocalString(sample.timestamp)
        }
    }

    private static utcApiLineEntryArrayToLocalTime(apiLineEntries: Array<ApiLineEntry>): Array<ApiLineEntry> {
        return apiLineEntries.map(d => this.utcApiLineEntryToLocalTime(d)).sort()
    }

    private static utcApiLineEntryToLocalTime(apiLineEntry: ApiLineEntry): ApiLineEntry {
        return {
            ...apiLineEntry,
            timestamp: this.utcToLocalString(apiLineEntry.timestamp)
        }
    }

    private static utcApiSampleArrayToLocalTime(apiSamples: Array<ApiSample>): Array<ApiSample> {
        return apiSamples.map(d => this.utcApiSampleToLocalTime(d)).sort();
    }

    private static utcToLocalString(timestamp: string): string {
        if (!timestamp.endsWith('Z')) {
            // Looks like this is not utc, so we don't change anything
            return timestamp;
        }
        return DateTime.fromISO(timestamp, {zone: 'utc'}).toLocal().toString();
    }
}
